package com.saas.cloud.common.websocket.sender;

/**
 * WebSocket 消息发送接口
 * <p>抽象消息发送逻辑，支持本地发送和集群广播两种实现。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface WebSocketMessageSender {

    /**
     * 向指定用户发送消息
     *
     * @param userId  用户 ID
     * @param message 消息内容（JSON 字符串）
     */
    void sendToUser(Long userId, String message);

    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容（JSON 字符串）
     */
    void broadcast(String message);
}
