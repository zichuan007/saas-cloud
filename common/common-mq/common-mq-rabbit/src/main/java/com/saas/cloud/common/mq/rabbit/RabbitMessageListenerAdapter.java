package com.saas.cloud.common.mq.rabbit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageConsumer;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 监听适配器
 * <p>桥接 AMQP {@link MessageListener} 与本项目 SPI：解析 headers、还原租户、
 * 按 payloadType 反序列化、组装信封、回调业务监听器，finally 清租户。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
public class RabbitMessageListenerAdapter implements MessageListener {

    private final com.saas.cloud.common.mq.MessageListener<?> bizListener;

    private final MqConsumer meta;

    private final ObjectMapper objectMapper;

    /**
     * 构造适配器
     *
     * @param bizListener  业务监听器
     * @param meta         @MqConsumer 元数据
     * @param objectMapper JSON 反序列化
     */
    public RabbitMessageListenerAdapter(com.saas.cloud.common.mq.MessageListener<?> bizListener,
                                        MqConsumer meta, ObjectMapper objectMapper) {
        this.bizListener = bizListener;
        this.meta = meta;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message) {
        Map<String, String> headers = toHeaders(message.getMessageProperties());
        try {
            restoreTenant(headers);
            Object data = deserialize(message.getBody(), bizListener.payloadType());
            MessageEnvelope envelope = MessageEnvelope.builder()
                    .msgId(headers.get(MqConst.HEADER_MSG_ID))
                    .bizId(headers.get(MqConst.HEADER_BIZ_ID))
                    .topic(meta.topic())
                    .data(data)
                    .headers(headers)
                    .build();
            MessageConsumer ctx = new RabbitMessageConsumer(message, headers);
            bizListener.onMessage(envelope, ctx);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 还原租户上下文
     *
     * @param headers 头 map
     */
    private void restoreTenant(Map<String, String> headers) {
        String tenantId = headers.get(MqConst.HEADER_TENANT_ID);
        if (tenantId == null || tenantId.isEmpty()) {
            return;
        }
        try {
            TenantContext.TenantInfo info = new TenantContext.TenantInfo();
            info.setTenantId(Long.valueOf(tenantId));
            TenantContext.set(info);
        } catch (NumberFormatException e) {
            log.warn("[MQ-Rabbit] 非法租户头 {}, 跳过还原", tenantId);
        }
    }

    /**
     * 反序列化 payload
     *
     * @param body         AMQP body
     * @param payloadType  目标类型
     * @return 反序列化对象
     */
    private Object deserialize(byte[] body, Class<?> payloadType) {
        if (body == null) {
            return null;
        }
        String text = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        if (payloadType == null || payloadType == String.class || payloadType == Object.class) {
            return text;
        }
        try {
            return objectMapper.readValue(text, payloadType);
        } catch (Exception e) {
            log.error("[MQ-Rabbit] payload 反序列化失败 type={}, value={}", payloadType, text, e);
            throw new IllegalArgumentException("payload 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * AMQP headers 转为 String map
     *
     * @param properties AMQP 消息属性
     * @return 头 map
     */
    private Map<String, String> toHeaders(MessageProperties properties) {
        Map<String, String> map = new HashMap<>();
        if (properties == null) {
            return map;
        }
        Map<String, Object> headers = properties.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> map.put(k, v == null ? null : v.toString()));
        }
        return map;
    }
}
