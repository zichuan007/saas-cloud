package com.saas.cloud.common.mq.rabbit;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.amqp.core.Message;

import com.saas.cloud.common.mq.MessageConsumer;

/**
 * RabbitMQ 消费上下文实现
 * <p>AckMode 为 AUTO 时容器自动确认；nack 抛异常触发重新入队/错误处理。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public class RabbitMessageConsumer implements MessageConsumer {

    private final Message message;

    private final Map<String, String> headers;

    /**
     * 构造消费上下文
     *
     * @param message AMQP 消息
     * @param headers 已解析的头 map
     */
    public RabbitMessageConsumer(Message message, Map<String, String> headers) {
        this.message = message;
        this.headers = headers;
    }

    @Override
    public String getPayload() {
        byte[] body = message.getBody();
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void ack() {
        // AckMode=AUTO 时容器自动确认
    }

    @Override
    public void nack(Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new RuntimeException(cause);
    }
}
