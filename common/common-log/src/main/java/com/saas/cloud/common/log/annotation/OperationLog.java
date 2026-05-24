package com.saas.cloud.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    String module() default "";

    String operation() default "";

    /**
     * 操作类型
     */
    OperateType type() default OperateType.OTHER;
}
