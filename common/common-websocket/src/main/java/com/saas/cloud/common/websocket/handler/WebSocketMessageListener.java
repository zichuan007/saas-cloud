package com.saas.cloud.common.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 消息监听器接口
 * <p>业务方实现此接口处理客户端发来的文本消息。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface WebSocketMessageListener {

    /**
     * 处理客户端消息
     *
     * @param userId  发送消息的用户 ID
     * @param message 消息内容（JSON 字符串）
     * @param session WebSocket 会话
     */
    void onMessage(Long userId, String message, WebSocketSession session);
}
