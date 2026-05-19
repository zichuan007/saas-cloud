package com.saas.cloud.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gateway 全局认证过滤器
 * <p>
 * 拦截所有请求，校验 JWT Token 有效性，将用户信息透传到下游服务请求头中。
 * 白名单路径直接放行，无需携带 Token。
 * </p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayJwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    private SecretKey secretKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径，无需 Token 认证
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/rbac/auth/login",
            "/api/rbac/auth/refresh",
            "/api/rbac/auth/register",
            "/api/platform/auth/login",
            "/api/generator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**"
    );

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhiteListed(path)) {
            log.debug("白名单路径放行: {}", path);
            return chain.filter(exchange);
        }

        // 从 Authorization 请求头提取 Bearer Token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            log.warn("请求缺少有效的 Authorization 头: {}", path);
            return unauthorizedResponse(exchange, "未提供有效的认证令牌");
        }

        String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());

        // 解析 JWT Token
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期, path: {}", path);
            return unauthorizedResponse(exchange, "Token无效或已过期");
        } catch (Exception e) {
            log.warn("Token 解析失败, path: {}, error: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "Token无效或已过期");
        }

        // 从 Claims 中提取用户信息，透传到下游服务请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(SecurityConstants.HEADER_USER_ID, toStringOrEmpty(claims.get("userId")))
                .header(SecurityConstants.HEADER_USERNAME, toStringOrEmpty(claims.getSubject()))
                .header(SecurityConstants.HEADER_TENANT_ID, toStringOrEmpty(claims.get("tenantId")))
                .header(SecurityConstants.HEADER_DEPT_ID, toStringOrEmpty(claims.get("deptId")))
                .header(SecurityConstants.HEADER_ROLE_LEVEL, toStringOrEmpty(claims.get("roleLevel")))
                .header(SecurityConstants.HEADER_DATA_SCOPE, toStringOrEmpty(claims.get("dataScope")))
                .header(SecurityConstants.HEADER_PERMISSIONS, convertPermissions(claims.get("permissions")))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 判断请求路径是否在白名单中
     *
     * @param path 请求路径
     * @return true-在白名单中, false-不在
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 构建 401 未授权 JSON 响应
     *
     * @param exchange ServerWebExchange
     * @param message  错误消息
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            bytes = "{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 将对象安全转换为字符串，null 时返回空字符串
     *
     * @param value 待转换对象
     * @return 字符串值
     */
    private String toStringOrEmpty(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    /**
     * 将 permissions 集合转换为逗号分隔字符串
     *
     * @param permissions permissions claim 值
     * @return 逗号分隔的权限字符串
     */
    @SuppressWarnings("unchecked")
    private String convertPermissions(Object permissions) {
        if (permissions == null) {
            return "";
        }
        if (permissions instanceof Collection) {
            return ((Collection<Object>) permissions).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return String.valueOf(permissions);
    }
}
