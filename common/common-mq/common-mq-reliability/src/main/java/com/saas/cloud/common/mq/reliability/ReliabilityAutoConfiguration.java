package com.saas.cloud.common.mq.reliability;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.mq.MqProperties;
import com.saas.cloud.common.mq.reliability.mapper.MqConsumeLogMapper;
import com.saas.cloud.common.mq.reliability.mapper.MqOutboxMapper;

import org.mybatis.spring.annotation.MapperScan;

/**
 * MQ 可靠性模式自动装配
 * <p>仅在 classpath 存在 MyBatis-Plus 时激活，注册 outbox/consume_log Mapper。
 * Outbox 与幂等分别由 {@code saas.mq.outbox.enabled} / {@code saas.mq.idempotent.enabled} 开关，默认关。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BaseMapper.class)
@MapperScan("com.saas.cloud.common.mq.reliability.mapper")
@EnableScheduling
public class ReliabilityAutoConfiguration {

    /**
     * Outbox 装饰器：启用时作为 @Primary MessageSender，生产侧注入即得可靠投递
     *
     * @param delegate     底层 Kafka MessageSender
     * @param outboxMapper outbox mapper
     * @param objectMapper JSON 序列化
     * @param properties   MQ 配置
     * @return Outbox 装饰器
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "saas.mq.outbox", name = "enabled", havingValue = "true")
    public OutboxMessageSender outboxMessageSender(
            @Qualifier("delegateMessageSender") MessageSender delegate,
            MqOutboxMapper outboxMapper,
            ObjectMapper objectMapper,
            MqProperties properties) {
        return new OutboxMessageSender(delegate, outboxMapper, objectMapper, properties);
    }

    /**
     * Outbox 补偿重试 Job
     *
     * @param delegate          底层 Kafka MessageSender（实投）
     * @param outboxMapper      outbox mapper
     * @param outboxMessageSender 状态更新助手
     * @param properties        MQ 配置
     * @return 补偿 Job
     */
    @Bean
    @ConditionalOnProperty(prefix = "saas.mq.outbox", name = "enabled", havingValue = "true")
    public OutboxRetryJob outboxRetryJob(
            @Qualifier("delegateMessageSender") MessageSender delegate,
            MqOutboxMapper outboxMapper,
            OutboxMessageSender outboxMessageSender,
            MqProperties properties) {
        return new OutboxRetryJob(delegate, outboxMapper, outboxMessageSender, properties);
    }

    /**
     * 消费幂等切面
     *
     * @param consumeLogMapper 幂等日志 mapper
     * @return 切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "saas.mq.idempotent", name = "enabled", havingValue = "true")
    public MqIdempotentAspect mqIdempotentAspect(MqConsumeLogMapper consumeLogMapper) {
        return new MqIdempotentAspect(consumeLogMapper);
    }
}
