package com.saas.cloud.common.mq.rocket;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;

import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

/**
 * RocketMQ 适配器自动装配
 * <p>仅当 {@code saas.mq.type=rocket} 且 classpath 存在 {@link RocketMQTemplate} 时激活。
 * 发送器 Bean 名 {@code delegateMessageSender}，与 Kafka/Rabbit 适配器互斥（由 type 闸门保证）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "saas.mq", name = "type", havingValue = "rocket")
public class RocketMqAutoConfiguration {

    /**
     * Rocket 适配器 Bean（与 Kafka/Rabbit 互斥，故可同名）
     *
     * @param rocketMQTemplate Rocket 模板
     * @param objectMapper     JSON 序列化
     * @return Rocket MessageSender 实现
     */
    @Bean("delegateMessageSender")
    @ConditionalOnClass(RocketMQTemplate.class)
    public MessageSender delegateMessageSender(RocketMQTemplate rocketMQTemplate,
                                              ObjectMapper objectMapper) {
        return new RocketMessageSender(rocketMQTemplate, objectMapper);
    }

    /**
     * Rocket 消费者注册表
     *
     * @param rocketMQTemplate          Rocket 模板（取 nameserver）
     * @param rocketMQPropertiesProvider RocketMQ 配置（备选 nameserver）
     * @param objectMapperProvider      JSON 反序列化
     * @param applicationContext        应用上下文
     * @return 注册表
     */
    @Bean
    public RocketListenerRegistrar rocketListenerRegistrar(
            RocketMQTemplate rocketMQTemplate,
            ObjectProvider<RocketMQProperties> rocketMQPropertiesProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ApplicationContext applicationContext) {
        return new RocketListenerRegistrar(rocketMQTemplate, rocketMQPropertiesProvider,
                objectMapperProvider, applicationContext);
    }
}
