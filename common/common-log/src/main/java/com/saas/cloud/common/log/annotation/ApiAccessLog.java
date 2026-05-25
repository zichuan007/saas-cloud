package com.saas.cloud.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 访问日志控制注解
 * <p>用于 Controller 方法上，按接口粒度控制
 * {@link com.saas.cloud.common.log.apilog.ApiAccessLogFilter} 的记录行为。</p>
 * <p>未标注此注解的接口保持默认行为（记录日志）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAccessLog {

    /**
     * 是否记录访问日志，设为 false 跳过此接口的日志记录
     */
    boolean enable() default true;

    /**
     * 是否记录请求参数（queryString），敏感接口可设为 false
     */
    boolean logArgs() default true;
}
