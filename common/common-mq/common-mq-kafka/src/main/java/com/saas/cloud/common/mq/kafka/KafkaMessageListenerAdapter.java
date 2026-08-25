package com.saas.cloud.common.mq.kafka;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageConsumer;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 监听适配器
 * <p>桥接 Spring Kafka 的 {@code MessageListener} 与本项目 SPI {@link com.saas.cloud.common.mq.MessageListener}：
 * 从 ConsumerRecord 解析头（租户/msgId/bizId）、还原租户上下文、按 payloadType 反序列化、
 * 组装信封后回调业务监听器，finally 清理租户上下文。</p>
 *
 * <p>value 统一归一化为 String，规避不同 deserializer 配置（byte[]/String）的差异。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaMessageListenerAdapter implements org.springframework.kafka.listener.MessageListener<Object, Object> {

    private final com.saas.cloud.common.mq.MessageListener<?> bizListener;

    private final MqConsumer meta;

    private final ObjectMapper objectMapper;

    /**
     * 构造适配器
     *
     * @param bizListener   业务监听器
     * @param meta          @MqConsumer 元数据
     * @param objectMapper JSON 反序列化
     */
    public KafkaMessageListenerAdapter(com.saas.cloud.common.mq.MessageListener<?> bizListener,
                                       MqConsumer meta, ObjectMapper objectMapper) {
        this.bizListener = bizListener;
        this.meta = meta;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(ConsumerRecord<Object, Object> record) {
        Map<String, String> headers = KafkaMessageConsumer.toMap(record);
        try {
            restoreTenant(headers);
            Object data = deserialize(record.value(), bizListener.payloadType());
            MessageEnvelope envelope = MessageEnvelope.builder()
                    .msgId(headers.get(MqConst.HEADER_MSG_ID))
                    .bizId(headers.get(MqConst.HEADER_BIZ_ID))
                    .topic(meta.topic())
                    .data(data)
                    .headers(headers)
                    .build();
            MessageConsumer ctx = new KafkaMessageConsumer(record, headers);
            bizListener.onMessage(envelope, ctx);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从头还原租户上下文（生产者注入的 X-Tenant-Id）
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
            log.warn("[MQ-Kafka] 非法租户头 {}, 跳过还原", tenantId);
        }
    }

    /**
     * 反序列化 payload：String/未指定类型直接返回文本，否则按 payloadType 反序列化
     *
     * @param value       原始 value
     * @param payloadType 目标类型
     * @return 反序列化对象
     */
    private Object deserialize(Object value, Class<?> payloadType) {
        String text = toText(value);
        if (payloadType == null || payloadType == String.class || payloadType == Object.class) {
            return text;
        }
        try {
            return objectMapper.readValue(text, payloadType);
        } catch (Exception e) {
            log.error("[MQ-Kafka] payload 反序列化失败 type={}, value={}", payloadType, text, e);
            throw new IllegalArgumentException("payload 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将任意 value 归一化为 String
     *
     * @param value 原始 value
     * @return 文本
     */
    static String toText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        if (value instanceof ByteBuffer) {
            return new String(((ByteBuffer) value).array(), StandardCharsets.UTF_8);
        }
        return value.toString();
    }
}
