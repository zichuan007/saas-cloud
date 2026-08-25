package com.saas.cloud.common.mq.kafka;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import com.saas.cloud.common.mq.MessageConsumer;

/**
 * Kafka 消费上下文实现
 * <p>封装 ConsumerRecord 的报文与头，ack/nack 在手动容器下映射为：
 * ack 无操作（容器按 AckMode 自动确认）；nack 抛出异常触发错误处理器（重试/死信）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public class KafkaMessageConsumer implements MessageConsumer {

    private final ConsumerRecord<?, ?> record;

    private final Map<String, String> headers;

    /**
     * 构造消费上下文
     *
     * @param record  Kafka 消费记录
     * @param headers 已解析的头 map
     */
    public KafkaMessageConsumer(ConsumerRecord<?, ?> record, Map<String, String> headers) {
        this.record = record;
        this.headers = headers;
    }

    @Override
    public String getPayload() {
        return KafkaMessageListenerAdapter.toText(record.value());
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void ack() {
        // 手动容器按 AckMode 自动确认，无需显式 ack
    }

    @Override
    public void nack(Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new RuntimeException(cause);
    }

    /**
     * 从 ConsumerRecord 头解析为 map（包含 X-Tenant-Id/X-Msg-Id 等）
     *
     * @param record 消费记录
     * @return 头 map
     */
    @SuppressWarnings("unused")
    public static Map<String, String> toMap(ConsumerRecord<?, ?> record) {
        Headers headers = record.headers();
        Map<String, String> map = new HashMap<>();
        for (Header header : headers) {
            map.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
        }
        return map;
    }
}
