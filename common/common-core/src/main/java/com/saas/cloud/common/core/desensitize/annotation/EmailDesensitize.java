package com.saas.cloud.common.core.desensitize.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.saas.cloud.common.core.desensitize.DesensitizeBy;
import com.saas.cloud.common.core.desensitize.handler.EmailDesensitizeHandler;

/**
 * 邮箱脱敏注解
 * <p>脱敏规则：邮箱前缀保留前 2 个字符，其余用 **** 替代，@ 及域名保留。
 * 例如：te****@gmail.com</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DesensitizeBy(handler = EmailDesensitizeHandler.class)
public @interface EmailDesensitize {
}
