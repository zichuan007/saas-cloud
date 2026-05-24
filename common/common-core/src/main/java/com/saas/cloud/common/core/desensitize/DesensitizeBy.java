package com.saas.cloud.common.core.desensitize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 数据脱敏元注解
 * <p>自定义脱敏注解（如 @MobileDesensitize）需要在其上标注此注解，
 * 指定对应的 {@link DesensitizeHandler} 实现类。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface DesensitizeBy {

    /**
     * 脱敏处理器类
     */
    @SuppressWarnings("rawtypes")
    Class<? extends DesensitizeHandler> handler();
}
