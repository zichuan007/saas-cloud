package com.saas.cloud.common.redis.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解，基于 Redisson 实现
 * <p>
 * key 支持 SpEL 表达式，例如：{@code @DistributedLock(key = "'user:' + #userId")}
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 锁的 key，支持 SpEL 表达式
     */
    String key();

    /**
     * 等待获取锁的最大时间，默认 3 秒
     */
    long waitTime() default 3;

    /**
     * 持有锁的最大时间（自动释放），默认 10 秒
     */
    long leaseTime() default 10;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时的提示消息
     */
    String failMessage() default "操作过于频繁，请稍后再试";
}
