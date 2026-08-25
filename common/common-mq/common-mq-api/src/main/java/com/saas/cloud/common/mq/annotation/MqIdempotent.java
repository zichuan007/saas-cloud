package com.saas.cloud.common.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费幂等标记
 * <p>标注于 {@link com.saas.cloud.common.mq.MessageListener#onMessage} 方法，
 * 由 {@code MqIdempotentAspect} 按幂等键查/插 {@code mq_consume_log} 去重。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqIdempotent {

    /**
     * 幂等键 SpEL，根对象为 {@code msg}（MessageEnvelope），缺省取 msgId
     *
     * @return SpEL 表达式
     */
    String key() default "#msg.msgId";

    /**
     * 幂等失效时间(秒)，0 表示永久
     *
     * @return 秒数
     */
    long expireSeconds() default 0;
}
