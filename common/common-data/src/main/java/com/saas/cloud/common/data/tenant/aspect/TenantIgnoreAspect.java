package com.saas.cloud.common.data.tenant.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.security.context.TenantContext;

/**
 * {@link TenantIgnore} 注解的 AOP 切面
 * 在方法执行前设置忽略租户过滤，执行后恢复原始状态。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Aspect
@Component
public class TenantIgnoreAspect {

    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        boolean original = TenantContext.isIgnoreTenant();
        try {
            TenantContext.setIgnoreTenant(true);
            return joinPoint.proceed();
        } finally {
            if (!original) {
                TenantContext.clearIgnoreTenant();
            }
        }
    }
}
