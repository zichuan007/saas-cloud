package com.saas.cloud.common.security.util;

import com.saas.cloud.common.security.config.JwtProperties;
import com.saas.cloud.common.security.context.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * JWT 工具类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JwtUtils {

    private final JwtProperties properties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserContext.UserInfo userInfo) {
        return buildToken(userInfo, properties.getAccessTokenExpire(), "access");
    }

    public String generateRefreshToken(UserContext.UserInfo userInfo) {
        return buildToken(userInfo, properties.getRefreshTokenExpire(), "refresh");
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token 已过期");
        } catch (Exception e) {
            log.debug("Token 无效: {}", e.getMessage());
        }
        return false;
    }

    public UserContext.UserInfo extractUserInfo(String token) {
        Claims claims = parseToken(token);
        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(claims.get("userId", Long.class));
        userInfo.setUsername(claims.getSubject());
        userInfo.setTenantId(claims.get("tenantId", Long.class));
        userInfo.setDeptId(claims.get("deptId", Long.class));
        userInfo.setRoleLevel(claims.get("roleLevel", Integer.class));
        userInfo.setDataScope(claims.get("dataScope", Integer.class));
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get("permissions", List.class);
        userInfo.setPermissions(permissions != null ? new HashSet<>(permissions) : new HashSet<>());
        return userInfo;
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        return "refresh".equals(claims.get("type", String.class));
    }

    public String getTokenId(String token) {
        return parseToken(token).getId();
    }

    private String buildToken(UserContext.UserInfo userInfo, long expireSeconds, String type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString().replace("-", ""))
                .setSubject(userInfo.getUsername())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireSeconds * 1000))
                .claim("userId", userInfo.getUserId())
                .claim("tenantId", userInfo.getTenantId())
                .claim("deptId", userInfo.getDeptId())
                .claim("roleLevel", userInfo.getRoleLevel())
                .claim("dataScope", userInfo.getDataScope())
                .claim("permissions", userInfo.getPermissions())
                .claim("type", type)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
