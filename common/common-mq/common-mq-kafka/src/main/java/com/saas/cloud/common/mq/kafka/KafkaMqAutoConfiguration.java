package com.saas.cloud.common.mq.kafka;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;

/**
 * Kafka 适配器自动装配
 * <p>仅当 {@code saas.mq.type=kafka}（缺省）且 classpath 存在 {@link KafkaTemplate} 时激活。
 * 发送器 Bean 名 {@code delegateMessageSender}，供 Outbox 装饰器以与底座无关的方式注入。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "saas.mq", name = "type", havingValue = "kafka", matchIfMissing = true)
public class KafkaMqAutoConfiguration {

    /**
     * Kafka 适配器 Bean
     *
     * @param kafkaTemplate Kafka 模板
     * @param objectMapper  JSON 序列化
     * @return Kafka MessageSender 实现
     */
    @Bean("delegateMessageSender")
    @ConditionalOnBean(KafkaTemplate.class)
    public MessageSender delegateMessageSender(KafkaTemplate<String, String> kafkaTemplate,
                                              ObjectMapper objectMapper) {
        return new KafkaMessageSender(kafkaTemplate, objectMapper);
    }

    /**
     * Kafka 消费者注册表：扫描 @MqConsumer 的 MessageListener Bean，构建容器替代 @KafkaListener
     *
     * @param consumerFactory     消费工厂
     * @param errorHandlerProvider 死信/重试错误处理器（可选）
     * @param objectMapperProvider JSON 反序列化
     * @param applicationContext  应用上下文
     * @return 注册表
     */
    @Bean
    @ConditionalOnBean(ConsumerFactory.class)
    public KafkaListenerRegistrar kafkaListenerRegistrar(
            ConsumerFactory<Object, Object> consumerFactory,
            ObjectProvider<CommonErrorHandler> errorHandlerProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ApplicationContext applicationContext) {
        return new KafkaListenerRegistrar(consumerFactory, errorHandlerProvider,
                objectMapperProvider, applicationContext);
    }
}
