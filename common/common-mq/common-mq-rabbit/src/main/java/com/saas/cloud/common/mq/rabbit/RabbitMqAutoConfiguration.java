package com.saas.cloud.common.mq.rabbit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ 适配器自动装配
 * <p>仅当 {@code saas.mq.type=rabbit} 且 classpath 存在 {@link RabbitTemplate} 时激活。
 * 发送器 Bean 名 {@code delegateMessageSender}，与 Kafka/Rocket 适配器互斥（由 type 闸门保证）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "saas.mq", name = "type", havingValue = "rabbit")
public class RabbitMqAutoConfiguration {

    /**
     * Rabbit 适配器 Bean（与 Kafka/Rocket 互斥，故可同名）
     *
     * @param rabbitTemplate      Rabbit 模板
     * @param rabbitAdminProvider Rabbit 管理器（声明队列，可选）
     * @param objectMapper        JSON 序列化
     * @return Rabbit MessageSender 实现
     */
    @Bean("delegateMessageSender")
    public MessageSender delegateMessageSender(RabbitTemplate rabbitTemplate,
                                               ObjectProvider<RabbitAdmin> rabbitAdminProvider,
                                               ObjectMapper objectMapper) {
        return new RabbitMessageSender(rabbitTemplate, rabbitAdminProvider, objectMapper);
    }

    /**
     * Rabbit 消费者注册表
     *
     * @param connectionFactory    连接工厂
     * @param rabbitAdminProvider   Rabbit 管理器（声明队列）
     * @param objectMapperProvider JSON 反序列化
     * @param applicationContext    应用上下文
     * @return 注册表
     */
    @Bean
    public RabbitListenerRegistrar rabbitListenerRegistrar(
            ConnectionFactory connectionFactory,
            ObjectProvider<RabbitAdmin> rabbitAdminProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ApplicationContext applicationContext) {
        return new RabbitListenerRegistrar(connectionFactory, rabbitAdminProvider,
                objectMapperProvider, applicationContext);
    }
}
