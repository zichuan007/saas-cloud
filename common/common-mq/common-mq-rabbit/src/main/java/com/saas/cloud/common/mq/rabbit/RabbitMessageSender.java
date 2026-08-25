package com.saas.cloud.common.mq.rabbit;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.SendResult;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 适配器：{@link MessageSender} 实现
 * <p>wire 格式与 Kafka 适配器对齐——envelope.data 序列化为消息体（JSON），
 * msgId/bizId/tenantId 进 AMQP headers，默认交换机路由到名为 topic 的队列。
 * 消费侧 {@link RabbitListenerRegistrar} 声明并消费同名队列。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMessageSender implements MessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectProvider<RabbitAdmin> rabbitAdminProvider;

    private final ObjectMapper objectMapper;

    /** 已声明的队列缓存，避免每次发送重复声明 */
    private final Set<String> declaredQueues = ConcurrentHashMap.newKeySet();

    @Override
    public <T> SendResult send(MessageEnvelope<T> msg) {
        ensureMsgId(msg);
        String topic = msg.getTopic();
        try {
            declareQueueIfNeeded(topic);
            String payload = serialize(msg.getData());
            Message message = MessageBuilder
                    .withBody(payload.getBytes(StandardCharsets.UTF_8))
                    .copyHeaders(buildHeaders(msg))
                    .build();
            rabbitTemplate.send(topic, message);
            log.debug("[MQ-Rabbit] 发送成功 topic={}, msgId={}", topic, msg.getMsgId());
            return SendResult.success(msg.getMsgId());
        } catch (Exception ex) {
            log.error("[MQ-Rabbit] 发送失败 topic={}, msgId={}, error={}",
                    topic, msg.getMsgId(), ex.getMessage(), ex);
            return SendResult.fail(msg.getMsgId(), ex.getMessage());
        }
    }

    @Override
    public <T> void sendReliable(MessageEnvelope<T> msg) {
        // outbox 未启用时退化为同步直投
        send(msg);
    }

    /**
     * 构造 AMQP headers：msgId/bizId/tenantId + 业务自定义头
     *
     * @param msg 信封
     * @return 头 map
     */
    private <T> Map<String, Object> buildHeaders(MessageEnvelope<T> msg) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MqConst.HEADER_MSG_ID, msg.getMsgId());
        if (msg.getBizId() != null) {
            headers.put(MqConst.HEADER_BIZ_ID, msg.getBizId());
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            headers.put(MqConst.HEADER_TENANT_ID, tenantId.toString());
        }
        if (msg.getHeaders() != null) {
            headers.putAll(msg.getHeaders());
        }
        return headers;
    }

    /**
     * 幂等声明队列（durable），默认交换机路由到同名队列
     *
     * @param topic 主题（= 队列名）
     */
    private void declareQueueIfNeeded(String topic) {
        if (declaredQueues.contains(topic)) {
            return;
        }
        RabbitAdmin admin = rabbitAdminProvider.getIfAvailable();
        if (admin != null) {
            try {
                admin.declareQueue(new org.springframework.amqp.core.Queue(topic, true));
                declaredQueues.add(topic);
            } catch (Exception e) {
                log.warn("[MQ-Rabbit] 声明队列失败 topic={}: {}", topic, e.getMessage());
            }
        }
    }

    /**
     * 确保信封有 msgId
     *
     * @param msg 信封
     */
    private <T> void ensureMsgId(MessageEnvelope<T> msg) {
        if (msg.getMsgId() == null || msg.getMsgId().isEmpty()) {
            msg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        }
    }

    /**
     * 序列化 payload，String 直接返回
     *
     * @param data 负载
     * @return JSON 字符串
     */
    private String serialize(Object data) throws JsonProcessingException {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return (String) data;
        }
        return objectMapper.writeValueAsString(data);
    }
}
