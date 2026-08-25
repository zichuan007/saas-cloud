package com.saas.cloud.common.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一消息信封
 * <p>对标 tuk-mq {@code MsgDetailDTO}：承载 msgId/bizId/topic/msgKey/data 与业务自定义头。
 * 租户头由适配器自动注入（{@link MqConst#HEADER_TENANT_ID}），业务无需填写。</p>
 *
 * @param <T> payload 类型
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEnvelope<T> {

    /** 消息唯一 ID，作为幂等键；缺省由发送侧生成 */
    private String msgId;

    /** 业务标识（如订单号），便于追踪，可空 */
    private String bizId;

    /** 目标主题 */
    private String topic;

    /** 分区/路由键，可空 */
    private String msgKey;

    /** 业务负载 */
    private T data;

    /** 业务自定义头，租户头由适配器注入，不在此显式设置 */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * 构造消息信封（无 msgKey）
     *
     * @param topic 主题
     * @param data  负载
     * @param <T>   负载类型
     * @return 信封
     */
    public static <T> MessageEnvelope<T> of(String topic, T data) {
        return of(topic, null, data);
    }

    /**
     * 构造消息信封（带 msgKey）
     *
     * @param topic  主题
     * @param msgKey 分区/路由键
     * @param data   负载
     * @param <T>    负载类型
     * @return 信封
     */
    public static <T> MessageEnvelope<T> of(String topic, String msgKey, T data) {
        return MessageEnvelope.<T>builder()
                .topic(topic)
                .msgKey(msgKey)
                .data(data)
                .build();
    }

    /**
     * 追加业务头
     *
     * @param key   头键
     * @param value 头值
     * @return 当前信封（链式）
     */
    public MessageEnvelope<T> header(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    /**
     * 只读视图，避免外部直接修改内部 map
     *
     * @return 不可修改头 map
     */
    public Map<String, String> readOnlyHeaders() {
        return headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(headers);
    }
}
