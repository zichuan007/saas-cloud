package com.saas.cloud.common.websocket.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 集群广播 WebSocket 消息发送实现
 * <p>通过 Kafka 将消息广播到所有服务实例，每个实例消费后在本地 SessionManager 发送。
 * 适用于多实例集群部署。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class KafkaWebSocketMessageSender implements WebSocketMessageSender {

    public static final String TOPIC_WS_BROADCAST = "saas-websocket-broadcast";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper;

    @Override
    public void sendToUser(Long userId, String message) {
        try {
            WebSocketBroadcastMessage broadcastMsg = new WebSocketBroadcastMessage();
            broadcastMsg.setUserId(userId);
            broadcastMsg.setMessage(message);
            kafkaTemplate.send(TOPIC_WS_BROADCAST, objectMapper.writeValueAsString(broadcastMsg));
        } catch (Exception e) {
            log.error("[WebSocket] Kafka 发送失败，降级本地发送: userId={}", userId, e);
            sessionManager.sendToUser(userId, message);
        }
    }

    @Override
    public void broadcast(String message) {
        try {
            WebSocketBroadcastMessage broadcastMsg = new WebSocketBroadcastMessage();
            broadcastMsg.setMessage(message);
            kafkaTemplate.send(TOPIC_WS_BROADCAST, objectMapper.writeValueAsString(broadcastMsg));
        } catch (Exception e) {
            log.error("[WebSocket] Kafka 广播失败，降级本地广播", e);
            sessionManager.broadcast(message);
        }
    }

    @KafkaListener(topics = TOPIC_WS_BROADCAST, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void onMessage(String json) {
        try {
            WebSocketBroadcastMessage msg = objectMapper.readValue(json, WebSocketBroadcastMessage.class);
            if (msg.getUserId() != null) {
                sessionManager.sendToUser(msg.getUserId(), msg.getMessage());
            } else {
                sessionManager.broadcast(msg.getMessage());
            }
        } catch (Exception e) {
            log.error("[WebSocket] 处理 Kafka 广播消息失败", e);
        }
    }

    /**
     * Kafka 广播消息体
     */
    @Data
    public static class WebSocketBroadcastMessage {

        /** 目标用户 ID（null 表示广播） */
        private Long userId;

        /** 消息内容 */
        private String message;
    }
}
