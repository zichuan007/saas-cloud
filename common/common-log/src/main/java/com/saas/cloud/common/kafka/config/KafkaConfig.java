package com.saas.cloud.common.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 主题配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_OPERATION_LOG = "saas-operation-log";
    public static final String TOPIC_NOTIFY_EVENT = "saas-notify-event";

    @Bean
    public NewTopic operationLogTopic() {
        return TopicBuilder.name(TOPIC_OPERATION_LOG)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notifyEventTopic() {
        return TopicBuilder.name(TOPIC_NOTIFY_EVENT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
