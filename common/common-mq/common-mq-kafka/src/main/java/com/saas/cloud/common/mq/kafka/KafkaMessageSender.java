package com.saas.cloud.common.mq.kafka;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

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
 * Kafka 适配器：{@link MessageSender} 实现
 * <p>wire 格式与现状一致——envelope.data 序列化为 value（StringSerializer），
 * msgId/bizId/tenantId 进 Kafka headers，确保旧消费者零改动、新消费者可读头做幂等。
 * 由 {@link KafkaMqAutoConfiguration} 声明为 Bean，避免跨包组件扫描失效。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class KafkaMessageSender implements MessageSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> SendResult send(MessageEnvelope<T> msg) {
        String msgId = ensureMsgId(msg);
        String topic = msg.getTopic();
        try {
            String payload = serialize(msg.getData());
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic, null, null, msg.getMsgKey(), payload, buildHeaders(msg, msgId));
            kafkaTemplate.send(record).get();
            log.debug("[MQ-Kafka] 发送成功 topic={}, msgId={}", topic, msgId);
            return SendResult.success(msgId);
        } catch (Exception ex) {
            log.error("[MQ-Kafka] 发送失败 topic={}, msgId={}, error={}", topic, msgId, ex.getMessage(), ex);
            return SendResult.fail(msgId, ex.getMessage());
        }
    }

    @Override
    public <T> void sendReliable(MessageEnvelope<T> msg) {
        // outbox 未启用时退化为同步直投；启用后由 OutboxMessageSender 装饰器覆盖
        send(msg);
    }

    /**
     * 构造 Kafka headers：msgId/bizId/tenantId + 业务自定义头
     *
     * @param msg   信封
     * @param msgId 消息 ID
     * @return Kafka headers
     */
    private <T> Headers buildHeaders(MessageEnvelope<T> msg, String msgId) {
        RecordHeaders headers = new RecordHeaders();
        headers.add(MqConst.HEADER_MSG_ID, msgId.getBytes(StandardCharsets.UTF_8));
        if (msg.getBizId() != null) {
            headers.add(MqConst.HEADER_BIZ_ID, msg.getBizId().getBytes(StandardCharsets.UTF_8));
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            headers.add(MqConst.HEADER_TENANT_ID, tenantId.toString().getBytes(StandardCharsets.UTF_8));
        }
        if (msg.getHeaders() != null) {
            msg.getHeaders().forEach((k, v) ->
                    headers.add(k, v.getBytes(StandardCharsets.UTF_8)));
        }
        return headers;
    }

    /**
     * 确保信封有 msgId，缺省生成
     *
     * @param msg 信封
     * @return msgId
     */
    private <T> String ensureMsgId(MessageEnvelope<T> msg) {
        if (msg.getMsgId() == null || msg.getMsgId().isEmpty()) {
            String id = UUID.randomUUID().toString().replace("-", "");
            msg.setMsgId(id);
            return id;
        }
        return msg.getMsgId();
    }

    /**
     * 序列化 payload，String 直接返回
     *
     * @param data 负载
     * @return JSON 字符串
     */
    private String serialize(Object data) throws JsonProcessingException {
        if (data instanceof String) {
            return (String) data;
        }
        return objectMapper.writeValueAsString(data);
    }
}
