package com.saas.cloud.common.websocket.session;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 会话管理器
 * <p>按 userId 维护在线连接，支持同一用户多设备连接。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class WebSocketSessionManager {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * 注册会话
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("[WebSocket] 用户连接: userId={}, sessionId={}, 在线用户数={}",
                userId, session.getId(), userSessions.size());
    }

    /**
     * 移除会话
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.info("[WebSocket] 用户断开: userId={}, sessionId={}, 在线用户数={}",
                userId, session.getId(), userSessions.size());
    }

    /**
     * 向指定用户发送消息（所有设备）
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    public void sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("[WebSocket] 发送消息失败: userId={}, sessionId={}", userId, session.getId(), e);
                }
            }
        }
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容
     */
    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        userSessions.forEach((userId, sessions) -> {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("[WebSocket] 广播消息失败: userId={}, sessionId={}", userId, session.getId(), e);
                    }
                }
            }
        });
    }

    /**
     * 获取指定用户的会话
     */
    public Set<WebSocketSession> getSessions(Long userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }

    /**
     * 获取所有在线用户 ID
     */
    public Collection<Long> getOnlineUserIds() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }
}
