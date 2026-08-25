package com.saas.cloud.common.mq;

import java.util.Map;

/**
 * 消费上下文
 * <p>封装底层报文与头，并提供 ack/nack 语义。租户上下文由适配器在进入
 * {@link MessageListener#onMessage} 前已写入 {@code TenantContext}。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public interface MessageConsumer {

    /**
     * 原始报文
     *
     * @return 报文字符串
     */
    String getPayload();

    /**
     * 底层头（含 {@link MqConst#HEADER_TENANT_ID}）
     *
     * @return 只读头 map
     */
    Map<String, String> getHeaders();

    /**
     * 确认消息
     */
    void ack();

    /**
     * 否决/重试
     *
     * @param cause 失败原因
     */
    void nack(Throwable cause);
}
