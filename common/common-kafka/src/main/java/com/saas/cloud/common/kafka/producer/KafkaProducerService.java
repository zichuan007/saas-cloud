package com.saas.cloud.common.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 消息发送服务
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object message) {
        kafkaTemplate.send(topic, message);
        log.debug("[Kafka] 发送消息到 topic={}", topic);
    }

    public void send(String topic, String key, Object message) {
        kafkaTemplate.send(topic, key, message);
        log.debug("[Kafka] 发送消息到 topic={}, key={}", topic, key);
    }
}
