package com.saas.cloud.rbac.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * WebSocket 服务端，按用户ID维护连接
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSIONS.put(userId, session);
        log.info("[WebSocket] 用户连接: userId={}, 在线人数={}", userId, SESSIONS.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSIONS.remove(userId);
        log.info("[WebSocket] 用户断开: userId={}, 在线人数={}", userId, SESSIONS.size());
    }

    @OnError
    public void onError(@PathParam("userId") String userId, Throwable error) {
        SESSIONS.remove(userId);
        log.error("[WebSocket] 连接异常: userId={}", userId, error);
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId  用户ID
     * @param message 消息内容（JSON 字符串）
     */
    public static void sendTo(String userId, String message) {
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("[WebSocket] 发送消息失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容（JSON 字符串）
     */
    public static void broadcast(String message) {
        SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("[WebSocket] 广播消息失败: userId={}", userId, e);
                }
            }
        });
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线人数
     */
    public static int getOnlineCount() {
        return SESSIONS.size();
    }
}
