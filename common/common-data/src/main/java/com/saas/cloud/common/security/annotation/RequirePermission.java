package com.saas.cloud.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解，标注在 Controller 方法上
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 权限标识，如 "sys:user:list"
     */
    String value();
}
