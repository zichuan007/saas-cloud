package com.saas.cloud.gateway.filter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.core.security.InternalSignatureService;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway 全局过滤器 —— 用户信息透传
 * 认证已由 SaReactorFilter 完成，本过滤器仅负责：
 * 1. 剥离外部伪造的内部调用标识头（含签名头）
 * 2. 从 Sa-Token Session 读取用户信息，透传到下游服务请求头
 * 3. 对透传的身份头做 HMAC 签名（X-Signature + X-Timestamp），下游校验防伪造
 *
 * @author saas-cloud
 * @version V3.0
 * @since 2026-07-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final InternalSignatureService signatureService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/rbac/auth/login",
            "/api/rbac/auth/refresh",
            "/api/rbac/auth/register",
            "/api/rbac/captcha/**",
            "/api/rbac/auth/social/**",
            "/api/platform/auth/login",
            "/api/wechat-oa/callback/**",
            "/api/generator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/api/rbac/ws/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(SecurityConstants.HEADER_INTERNAL_SOURCE);
                    h.remove(SecurityConstants.HEADER_INTERNAL_SIGNATURE);
                    h.remove(SecurityConstants.HEADER_INTERNAL_TIMESTAMP);
                })
                .build();
        exchange = exchange.mutate().request(request).build();

        String path = request.getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return chain.filter(exchange);
        }

        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            return chain.filter(exchange);
        }

        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId, false);
            if (session == null) {
                return chain.filter(exchange.mutate().request(request).build());
            }
            String userId = toStr(session.get("userId"));
            String username = toStr(session.get("username"));
            String tenantId = toStr(session.get("tenantId"));
            String deptId = toStr(session.get("deptId"));
            String roleLevel = toStr(session.get("roleLevel"));
            String dataScope = toStr(session.get("dataScope"));
            String permissions = toStr(session.get("permissions"));

            // 对透传的身份头做 HMAC 签名，下游据 X-Signature 校验防伪造
            long timestamp = System.currentTimeMillis();
            Map<String, String> userHeaders = new LinkedHashMap<>();
            userHeaders.put(SecurityConstants.HEADER_USER_ID, userId);
            userHeaders.put(SecurityConstants.HEADER_USERNAME, username);
            userHeaders.put(SecurityConstants.HEADER_TENANT_ID, tenantId);
            userHeaders.put(SecurityConstants.HEADER_DEPT_ID, deptId);
            userHeaders.put(SecurityConstants.HEADER_ROLE_LEVEL, roleLevel);
            userHeaders.put(SecurityConstants.HEADER_DATA_SCOPE, dataScope);
            userHeaders.put(SecurityConstants.HEADER_PERMISSIONS, permissions);
            String signature = signatureService.signUserHeaders(userHeaders, timestamp);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(SecurityConstants.HEADER_USER_ID, userId)
                    .header(SecurityConstants.HEADER_USERNAME, username)
                    .header(SecurityConstants.HEADER_TENANT_ID, tenantId)
                    .header(SecurityConstants.HEADER_DEPT_ID, deptId)
                    .header(SecurityConstants.HEADER_ROLE_LEVEL, roleLevel)
                    .header(SecurityConstants.HEADER_DATA_SCOPE, dataScope)
                    .header(SecurityConstants.HEADER_PERMISSIONS, permissions)
                    .header(SecurityConstants.HEADER_SIGNATURE, signature)
                    .header(SecurityConstants.HEADER_TIMESTAMP, String.valueOf(timestamp))
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.warn("从 Sa-Token Session 读取用户信息失败: {}", e.getMessage());
            return chain.filter(exchange.mutate().request(request).build());
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private String toStr(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
}
