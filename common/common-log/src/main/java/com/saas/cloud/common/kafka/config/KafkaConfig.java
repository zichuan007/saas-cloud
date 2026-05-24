package com.saas.cloud.common.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import com.saas.cloud.common.kafka.interceptor.TenantKafkaListenerInterceptor;
import com.saas.cloud.common.kafka.interceptor.TenantKafkaProducerInterceptor;

/**
 * Kafka 主题配置 + 多租户拦截器注册
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_OPERATION_LOG = "saas-operation-log";
    public static final String TOPIC_NOTIFY_EVENT = "saas-notify-event";
    public static final String TOPIC_API_ACCESS_LOG = "saas-api-access-log";
    public static final String TOPIC_API_ERROR_LOG = "saas-api-error-log";

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

    @Bean
    public NewTopic apiAccessLogTopic() {
        return TopicBuilder.name(TOPIC_API_ACCESS_LOG)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic apiErrorLogTopic() {
        return TopicBuilder.name(TOPIC_API_ERROR_LOG)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 注册 Producer 端租户拦截器
     */
    @Bean
    public DefaultKafkaProducerFactoryCustomizer tenantProducerCustomizer() {
        return producerFactory -> {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                    TenantKafkaProducerInterceptor.class.getName());
            producerFactory.updateConfigs(config);
        };
    }

    /**
     * 注册 Consumer 端租户 RecordInterceptor：每条消息处理前设置租户上下文
     */
    @Bean
    public ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>> tenantConsumerCustomizer() {
        return container -> container.setRecordInterceptor(new TenantKafkaListenerInterceptor());
    }
}
