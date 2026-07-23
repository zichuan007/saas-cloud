package com.saas.cloud.common.websocket.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.websocket.interceptor.LoginUserHandshakeInterceptor;
import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用 JSON 文本 WebSocket 处理器
 * <p>管理连接生命周期，将 userId 和 Session 注册到 SessionManager。
 * 收到的文本消息交给 {@link WebSocketMessageListener} 处理（如果已注册）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class JsonWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    private final WebSocketMessageListener messageListener;

    public JsonWebSocketHandler(WebSocketSessionManager sessionManager, WebSocketMessageListener messageListener) {
        this.sessionManager = sessionManager;
        this.messageListener = messageListener;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (messageListener == null) {
            return;
        }
        Long userId = getUserId(session);
        // WebSocket 消息线程无 HTTP 上下文，从握手属性还原租户上下文，
        // 避免消息处理逻辑查租户维度表时丢失租户隔离
        Long tenantId = getTenantId(session);
        if (tenantId != null) {
            TenantContext.TenantInfo info = new TenantContext.TenantInfo();
            info.setTenantId(tenantId);
            TenantContext.set(info);
        }
        try {
            messageListener.onMessage(userId, message.getPayload(), session);
        } finally {
            TenantContext.clear();
        }
    }

    private Long getTenantId(WebSocketSession session) {
        return (Long) session.getAttributes().get(LoginUserHandshakeInterceptor.ATTR_TENANT_ID);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = getUserId(session);
        log.error("[WebSocket] 传输异常: userId={}, sessionId={}", userId, session.getId(), exception);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    private Long getUserId(WebSocketSession session) {
        return (Long) session.getAttributes().get(LoginUserHandshakeInterceptor.ATTR_USER_ID);
    }
}
