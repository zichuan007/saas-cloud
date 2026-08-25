package com.saas.cloud.common.mq.rocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageConsumer;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * RocketMQ 监听适配器
 * <p>桥接 RocketMQ {@link MessageListenerConcurrently} 与本项目 SPI：每条 MessageExt 解析
 * userProperty（租户/msgId/bizId）、还原租户、按 payloadType 反序列化、组装信封、回调业务。
 * 任一异常返回 RECONSUME_LATER 触发重试，finally 清租户。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
public class RocketMessageListenerAdapter implements MessageListenerConcurrently {

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
    public RocketMessageListenerAdapter(com.saas.cloud.common.mq.MessageListener<?> bizListener,
                                        MqConsumer meta, ObjectMapper objectMapper) {
        this.bizListener = bizListener;
        this.meta = meta;
        this.objectMapper = objectMapper;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                     ConsumeConcurrentlyContext context) {
        if (msgs == null || msgs.isEmpty()) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        boolean failed = false;
        for (MessageExt ext : msgs) {
            try {
                handleOne(ext);
            } catch (Exception e) {
                failed = true;
                log.error("[MQ-Rocket] 消费失败 topic={}, msgId={}: {}",
                        ext.getTopic(), ext.getMsgId(), e.getMessage(), e);
            }
        }
        return failed ? ConsumeConcurrentlyStatus.RECONSUME_LATER
                : ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 处理单条消息
     *
     * @param ext RocketMQ 消息
     */
    private void handleOne(MessageExt ext) {
        Map<String, String> headers = toHeaders(ext);
        try {
            restoreTenant(headers);
            Object data = deserialize(ext.getBody(), bizListener.payloadType());
            MessageEnvelope envelope = MessageEnvelope.builder()
                    .msgId(firstNonBlank(headers.get(MqConst.HEADER_MSG_ID), ext.getMsgId()))
                    .bizId(headers.get(MqConst.HEADER_BIZ_ID))
                    .topic(meta.topic())
                    .data(data)
                    .headers(headers)
                    .build();
            MessageConsumer ctx = new RocketMessageConsumer(ext, headers);
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
            log.warn("[MQ-Rocket] 非法租户头 {}, 跳过还原", tenantId);
        }
    }

    /**
     * 反序列化 payload
     *
     * @param body         RocketMQ body
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
            log.error("[MQ-Rocket] payload 反序列化失败 type={}, value={}", payloadType, text, e);
            throw new IllegalArgumentException("payload 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * userProperty 转 String map
     *
     * @param ext RocketMQ 消息
     * @return 头 map
     */
    private Map<String, String> toHeaders(MessageExt ext) {
        Map<String, String> map = new HashMap<>();
        Map<String, String> props = ext.getProperties();
        if (props != null) {
            map.putAll(props);
        }
        return map;
    }

    /**
     * 取首个非空白串
     *
     * @param a   候选1
     * @param b   候选2
     * @return 首个非空白串
     */
    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b;
    }
}
