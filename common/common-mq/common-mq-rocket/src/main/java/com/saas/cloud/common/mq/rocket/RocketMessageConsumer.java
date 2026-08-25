package com.saas.cloud.common.mq.rocket;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.common.message.MessageExt;

import com.saas.cloud.common.mq.MessageConsumer;

/**
 * RocketMQ 消费上下文实现
 * <p>RocketMQ 消费确认通过监听器返回值（CONSUME_SUCCESS/RECONSUME_LATER），
 * 故 ack/nack 为语义占位：nack 抛异常供适配器捕获转 RECONSUME_LATER。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public class RocketMessageConsumer implements MessageConsumer {

    private final MessageExt messageExt;

    private final Map<String, String> headers;

    /**
     * 构造消费上下文
     *
     * @param messageExt RocketMQ 消息
     * @param headers    已解析的头 map
     */
    public RocketMessageConsumer(MessageExt messageExt, Map<String, String> headers) {
        this.messageExt = messageExt;
        this.headers = headers;
    }

    @Override
    public String getPayload() {
        byte[] body = messageExt.getBody();
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void ack() {
        // RocketMQ 通过返回值确认，无显式 ack
    }

    @Override
    public void nack(Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new RuntimeException(cause);
    }
}
