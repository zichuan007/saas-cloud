package com.saas.cloud.common.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 {@link com.saas.cloud.common.mq.MessageListener} 实现为 MQ 消费者
 * <p>跨 MQ 通用注解，替代 Spring 的 {@code @KafkaListener}。starter 启动时
 * 扫描所有 {@code @MqConsumer} Bean，按 {@code saas.mq.type} 注册到底层 MQ。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqConsumer {

    /**
     * 主题
     *
     * @return 主题名
     */
    String topic();

    /**
     * 消费组
     *
     * @return 消费组名
     */
    String group();

    /**
     * 并发度，0 表示用适配器默认
     *
     * @return 并发度
     */
    int concurrency() default 0;

    /**
     * 是否广播模式：每实例收全量消息（用于 WebSocket 集群广播）。
     * <p>Kafka→每实例随机 group；Rabbit→每实例独立队列；Rocket→MessageModel.BROADCASTING。</p>
     *
     * @return true 表示广播
     */
    boolean broadcast() default false;
}
