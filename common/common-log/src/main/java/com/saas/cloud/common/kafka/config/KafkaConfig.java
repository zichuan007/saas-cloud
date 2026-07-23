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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

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
     * 注册 Producer 端租户拦截器 + 可靠投递参数
     */
    @Bean
    public DefaultKafkaProducerFactoryCustomizer tenantProducerCustomizer() {
        return producerFactory -> {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                    TenantKafkaProducerInterceptor.class.getName());
            // 可靠投递：全部副本确认 + 幂等生产者 + 失败重试，防止消息丢失与重复
            config.put(ProducerConfig.ACKS_CONFIG, "all");
            config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            config.put(ProducerConfig.RETRIES_CONFIG, 3);
            config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
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

    /**
     * 消费端错误处理：消费失败重试 3 次（1 秒间隔），仍失败则投递到死信队列（topic + ".DLT"）。
     * <p>Spring Boot 自动将其装配到所有 @KafkaListener 容器。</p>
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }
}
