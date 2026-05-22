package com.saas.cloud.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 内部接口标识注解
 * <p>
 * 标注在 Controller 类或方法上，表示该接口仅供微服务间 Feign 内部调用。
 * 外部请求（不含 X-Internal-Source 头）将被 InnerApiAspect 拦截返回 403。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InnerApi {
}
