package com.saas.cloud.common.data.tenant.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略租户过滤注解
 * <p>
 * 标注在 Service/Controller 方法上，该方法内的所有 SQL 将不自动追加 tenant_id 条件。
 * 适用于平台管理端查询、跨租户统计等场景。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {
}
