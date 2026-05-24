package com.saas.cloud.common.websocket.sender;

import org.springframework.beans.factory.annotation.Autowired;

import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

import lombok.RequiredArgsConstructor;

/**
 * 本地 WebSocket 消息发送实现
 * <p>直接通过本地 SessionManager 发送消息，适用于单机部署。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LocalWebSocketMessageSender implements WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void sendToUser(Long userId, String message) {
        sessionManager.sendToUser(userId, message);
    }

    @Override
    public void broadcast(String message) {
        sessionManager.broadcast(message);
    }
}
