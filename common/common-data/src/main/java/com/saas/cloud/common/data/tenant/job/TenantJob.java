package com.saas.cloud.common.data.tenant.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多租户 Job 注解
 * <p>标注在 XxlJob 或 Scheduled 方法上，AOP 会自动遍历所有启用租户，
 * 逐个设置 TenantContext 后执行任务逻辑。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantJob {
}
