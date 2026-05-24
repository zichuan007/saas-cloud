package com.saas.cloud.rbac.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.websocket.sender.WebSocketMessageSender;
import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 服务端门面
 * <p>委托 common-websocket 框架的 SessionManager 和 MessageSender 实现。
 * 提供静态方法保持对外接口兼容。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@Component
public class WebSocketServer {

    private static WebSocketMessageSender messageSender;

    private static WebSocketSessionManager sessionManager;

    @Autowired
    public void init(WebSocketMessageSender sender, WebSocketSessionManager manager) {
        WebSocketServer.messageSender = sender;
        WebSocketServer.sessionManager = manager;
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId  用户ID
     * @param message 消息内容（JSON 字符串）
     */
    public static void sendTo(String userId, String message) {
        if (messageSender == null) {
            log.warn("[WebSocket] MessageSender 未初始化");
            return;
        }
        messageSender.sendToUser(Long.parseLong(userId), message);
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容（JSON 字符串）
     */
    public static void broadcast(String message) {
        if (messageSender == null) {
            log.warn("[WebSocket] MessageSender 未初始化");
            return;
        }
        messageSender.broadcast(message);
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线人数
     */
    public static int getOnlineCount() {
        return sessionManager != null ? sessionManager.getOnlineCount() : 0;
    }
}
