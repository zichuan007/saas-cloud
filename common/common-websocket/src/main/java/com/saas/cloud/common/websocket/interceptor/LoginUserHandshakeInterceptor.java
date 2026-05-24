package com.saas.cloud.common.websocket.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 握手拦截器
 * <p>在握手阶段从请求参数中提取 token 并解析用户信息，将 userId 存入 WebSocketSession 属性中。
 * Token 校验逻辑由具体应用实现 {@link WebSocketTokenResolver} 并注入。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class LoginUserHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_TENANT_ID = "tenantId";

    private final WebSocketTokenResolver tokenResolver;

    public LoginUserHandshakeInterceptor(WebSocketTokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("[WebSocket] 握手拒绝: 非 Servlet 请求");
            return false;
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null || token.isBlank()) {
            log.warn("[WebSocket] 握手拒绝: 缺少 token 参数");
            return false;
        }

        WebSocketTokenResolver.TokenInfo tokenInfo = tokenResolver.resolve(token);
        if (tokenInfo == null) {
            log.warn("[WebSocket] 握手拒绝: token 无效");
            return false;
        }

        attributes.put(ATTR_USER_ID, tokenInfo.getUserId());
        if (tokenInfo.getTenantId() != null) {
            attributes.put(ATTR_TENANT_ID, tokenInfo.getTenantId());
        }
        log.debug("[WebSocket] 握手成功: userId={}, tenantId={}", tokenInfo.getUserId(), tokenInfo.getTenantId());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }
}
