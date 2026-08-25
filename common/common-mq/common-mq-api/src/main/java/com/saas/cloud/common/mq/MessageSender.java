package com.saas.cloud.common.mq;

/**
 * 统一消息生产 SPI
 * <p>业务侧仅依赖此接口，由 {@code common-mq-spring-boot-starter} 按 {@code saas.mq.type}
 * 装配对应适配器实现。Outbox 可靠性模式以装饰器形式包装本接口。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public interface MessageSender {

    /**
     * 同步直投：不入 outbox，低延迟，调用方感知失败。
     * <p>适配器负责注入租户头、生成缺失的 msgId。</p>
     *
     * @param msg 消息信封
     * @param <T> 负载类型
     * @return 投递结果
     */
    <T> SendResult send(MessageEnvelope<T> msg);

    /**
     * 异步可靠投递：经 outbox 落库 + 后台实投，至少一次送达。
     * <p>未启用 outbox 时退化为 {@link #send} 同步直投。</p>
     *
     * @param msg 消息信封
     * @param <T> 负载类型
     */
    <T> void sendReliable(MessageEnvelope<T> msg);
}
