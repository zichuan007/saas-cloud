package com.saas.cloud.gateway.filter;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.saas.cloud.common.core.constant.SecurityConstants;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway 全局过滤器 —— 用户信息透传
 * 认证已由 SaReactorFilter 完成，本过滤器仅负责：
 * 1. 剥离外部伪造的内部调用标识头
 * 2. 从 Sa-Token Session 读取用户信息，透传到下游服务请求头
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-05-22
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/rbac/auth/login",
            "/api/rbac/auth/refresh",
            "/api/rbac/auth/register",
            "/api/rbac/captcha/**",
            "/api/platform/auth/login",
            "/api/generator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/actuator/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(h -> h.remove(SecurityConstants.HEADER_INTERNAL_SOURCE))
                .build();
        exchange = exchange.mutate().request(request).build();

        String path = request.getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        if (!StpUtil.isLogin()) {
            return chain.filter(exchange);
        }

        try {
            SaSession session = StpUtil.getSession();
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(SecurityConstants.HEADER_USER_ID, toStr(session.get("userId")))
                    .header(SecurityConstants.HEADER_USERNAME, toStr(session.get("username")))
                    .header(SecurityConstants.HEADER_TENANT_ID, toStr(session.get("tenantId")))
                    .header(SecurityConstants.HEADER_DEPT_ID, toStr(session.get("deptId")))
                    .header(SecurityConstants.HEADER_ROLE_LEVEL, toStr(session.get("roleLevel")))
                    .header(SecurityConstants.HEADER_DATA_SCOPE, toStr(session.get("dataScope")))
                    .header(SecurityConstants.HEADER_PERMISSIONS, toStr(session.get("permissions")))
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.warn("从 Sa-Token Session 读取用户信息失败: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String toStr(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
}
