package com.saas.cloud.common.redis.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解，基于 Redisson 防止重复提交
 * <p>
 * key 支持 SpEL 表达式，例如：{@code @Idempotent(key = "'order:' + #orderId")}
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等 key，支持 SpEL 表达式
     */
    String key();

    /**
     * 幂等时间窗口，默认 5 秒
     */
    long timeout() default 5;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 重复提交时的提示消息
     */
    String message() default "请勿重复提交";
}
