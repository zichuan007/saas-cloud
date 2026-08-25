package com.saas.cloud.common.websocket.sender;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageConsumer;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MessageListener;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 集群广播 MQ 实现
 * <p>生产侧经 {@link MessageSender} 广播到 {@link MqConst#TOPIC_WEBSOCKET_BROADCAST}，
 * 消费侧 {@code @MqConsumer(broadcast=true)} 每实例收全量后投递到本地 SessionManager。
 * 适用于多实例集群部署，与底层 MQ 类型无关（Kafka/Rabbit/Rocket 任一）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@MqConsumer(topic = MqConst.TOPIC_WEBSOCKET_BROADCAST, group = "websocket", broadcast = true)
public class MqWebSocketMessageSender implements WebSocketMessageSender,
        MessageListener<MqWebSocketMessageSender.WebSocketBroadcastMessage> {

    private final MessageSender messageSender;

    private final WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper;

    @Override
    public void sendToUser(Long userId, String message) {
        try {
            WebSocketBroadcastMessage broadcastMsg = new WebSocketBroadcastMessage();
            broadcastMsg.setUserId(userId);
            broadcastMsg.setMessage(message);
            messageSender.send(MessageEnvelope.of(MqConst.TOPIC_WEBSOCKET_BROADCAST, broadcastMsg));
        } catch (Exception e) {
            log.error("[WebSocket] MQ 发送失败，降级本地发送: userId={}", userId, e);
            sessionManager.sendToUser(userId, message);
        }
    }

    @Override
    public void broadcast(String message) {
        try {
            WebSocketBroadcastMessage broadcastMsg = new WebSocketBroadcastMessage();
            broadcastMsg.setMessage(message);
            messageSender.send(MessageEnvelope.of(MqConst.TOPIC_WEBSOCKET_BROADCAST, broadcastMsg));
        } catch (Exception e) {
            log.error("[WebSocket] MQ 广播失败，降级本地广播", e);
            sessionManager.broadcast(message);
        }
    }

    @Override
    public Class<WebSocketBroadcastMessage> payloadType() {
        return WebSocketBroadcastMessage.class;
    }

    @Override
    public void onMessage(MessageEnvelope<WebSocketBroadcastMessage> msg, MessageConsumer ctx) {
        WebSocketBroadcastMessage data = msg.getData();
        if (data == null) {
            return;
        }
        if (data.getUserId() != null) {
            sessionManager.sendToUser(data.getUserId(), data.getMessage());
        } else {
            sessionManager.broadcast(data.getMessage());
        }
    }

    /**
     * WebSocket 广播消息体
     */
    @Data
    public static class WebSocketBroadcastMessage {

        /** 目标用户 ID（null 表示广播） */
        private Long userId;

        /** 消息内容 */
        private String message;
    }
}
