package com.saas.cloud.common.mq.rocket;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

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
 * RocketMQ 适配器：{@link MessageSender} 实现
 * <p>使用底层 {@link org.apache.rocketmq.client.producer.DefaultMQProducer} 发送，
 * msgId/bizId/tenantId 显式写入 userProperty，确保消费侧可还原（不依赖消息转换器的头部映射）。
 * topic 作为 RocketMQ 主题，msgId 同时设为消息 keys 以便 broker 侧查询。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMessageSender implements MessageSender {

    private final RocketMQTemplate rocketMQTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public <T> SendResult send(MessageEnvelope<T> msg) {
        ensureMsgId(msg);
        String topic = msg.getTopic();
        try {
            byte[] body = serialize(msg.getData()).getBytes(StandardCharsets.UTF_8);
            Message rmq = new Message(topic, null, msg.getMsgId(), body);
            rmq.putUserProperty(MqConst.HEADER_MSG_ID, msg.getMsgId());
            if (msg.getBizId() != null) {
                rmq.putUserProperty(MqConst.HEADER_BIZ_ID, msg.getBizId());
            }
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                rmq.putUserProperty(MqConst.HEADER_TENANT_ID, tenantId.toString());
            }
            if (msg.getHeaders() != null) {
                msg.getHeaders().forEach(rmq::putUserProperty);
            }
            org.apache.rocketmq.client.producer.SendResult result = sendRocket(rmq);
            if (result != null && result.getSendStatus() == SendStatus.SEND_OK) {
                log.debug("[MQ-Rocket] 发送成功 topic={}, msgId={}", topic, msg.getMsgId());
                return SendResult.success(msg.getMsgId());
            }
            String error = result == null ? "null result" : result.getSendStatus().name();
            log.error("[MQ-Rocket] 发送未确认 topic={}, msgId={}, status={}", topic, msg.getMsgId(), error);
            return SendResult.fail(msg.getMsgId(), error);
        } catch (Exception ex) {
            log.error("[MQ-Rocket] 发送失败 topic={}, msgId={}, error={}",
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

    /**
     * 调用底层 producer 发送，返回 RocketMQ SendResult
     *
     * @param rmq RocketMQ 消息
     * @return RocketMQ 发送结果
     */
    private org.apache.rocketmq.client.producer.SendResult sendRocket(Message rmq) throws Exception {
        return rocketMQTemplate.getProducer().send(rmq);
    }
}
