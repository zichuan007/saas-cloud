package com.saas.cloud.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.constant.SecurityConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway 租户安全过滤器
 * <p>
 * 防止跨租户访问：校验请求中的租户身份与 JWT 解析出的租户身份是否一致。
 * 在 AuthGlobalFilter 之后执行（order = -90），此时 X-Tenant-Id 头已由 AuthGlobalFilter 设置。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantSecurityFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 不做跨租户校验的路径（平台管理端、白名单路径）
     */
    private static final List<String> IGNORE_PATHS = Arrays.asList(
            "/api/platform/**",
            "/api/rbac/auth/login",
            "/api/rbac/auth/refresh",
            "/api/rbac/auth/register",
            "/api/generator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 忽略的路径直接放行
        if (shouldIgnore(path)) {
            return chain.filter(exchange);
        }

        // 从 AuthGlobalFilter 透传的头中获取 JWT 解析出的租户ID（可信来源）
        String jwtTenantId = request.getHeaders().getFirst(SecurityConstants.HEADER_TENANT_ID);
        if (!StringUtils.hasText(jwtTenantId)) {
            return chain.filter(exchange);
        }

        // 检查请求参数中是否携带了 tenantId
        String queryTenantId = request.getQueryParams().getFirst("tenantId");
        if (StringUtils.hasText(queryTenantId) && !queryTenantId.equals(jwtTenantId)) {
            log.warn("跨租户访问拦截: path={}, jwtTenantId={}, queryTenantId={}", path, jwtTenantId, queryTenantId);
            return forbiddenResponse(exchange, "租户身份校验失败");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 在 AuthGlobalFilter(-100) 之后执行
        return -90;
    }

    private boolean shouldIgnore(String path) {
        return IGNORE_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", 403);
        result.put("message", message);
        result.put("data", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            bytes = "{\"code\":403,\"message\":\"租户身份校验失败\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
