package com.saas.cloud.common.mq;

/**
 * 统一消息消费 SPI
 * <p>业务实现此接口并注册为 Bean，标注 {@link com.saas.cloud.common.mq.annotation.MqConsumer}。
 * starter 启动时收集所有 listener，按 {@code saas.mq.type} 注册到底层 MQ。</p>
 *
 * @param <T> payload 类型
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public interface MessageListener<T> {

    /**
     * 主题，优先取 {@code @MqConsumer} 注解值，返回 null 表示用注解
     *
     * @return 主题
     */
    default String topic() {
        return null;
    }

    /**
     * 消费组，优先取注解值
     *
     * @return 消费组
     */
    default String group() {
        return null;
    }

    /**
     * 负载类型，供适配器反序列化
     *
     * @return 负载 Class
     */
    default Class<T> payloadType() {
        return null;
    }

    /**
     * 消息处理
     *
     * @param msg 消息信封（data 已按 payloadType 反序列化）
     * @param ctx 消费上下文
     */
    void onMessage(MessageEnvelope<T> msg, MessageConsumer ctx);
}
