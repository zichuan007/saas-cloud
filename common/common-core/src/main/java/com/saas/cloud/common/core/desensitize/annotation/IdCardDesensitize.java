package com.saas.cloud.common.core.desensitize.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.saas.cloud.common.core.desensitize.DesensitizeBy;
import com.saas.cloud.common.core.desensitize.handler.IdCardDesensitizeHandler;

/**
 * 身份证号脱敏注解
 * <p>脱敏规则：保留前 6 位和后 4 位，中间用 ******** 替代。
 * 例如：110101********1234</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DesensitizeBy(handler = IdCardDesensitizeHandler.class)
public @interface IdCardDesensitize {
}
