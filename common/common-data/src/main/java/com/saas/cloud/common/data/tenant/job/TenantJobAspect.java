package com.saas.cloud.common.data.tenant.job;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.security.context.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户 Job AOP 切面
 * <p>拦截 @TenantJob 注解的方法，自动遍历所有启用租户，
 * 逐个设置 TenantContext 后执行原始方法。</p>
 * <p>注意：被 @TenantJob 标注的 Job 方法需要保证幂等性，
 * 因为某个租户执行失败后重试时，之前成功的租户可能会再次执行。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@Aspect
@Component
@ConditionalOnBean(TenantFrameworkService.class)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantJobAspect {

    private final TenantFrameworkService tenantFrameworkService;

    @Around("@annotation(tenantJob)")
    public Object around(ProceedingJoinPoint joinPoint, TenantJob tenantJob) throws Throwable {
        List<Long> tenantIds = tenantFrameworkService.getActiveTenantIds();
        if (tenantIds == null || tenantIds.isEmpty()) {
            log.warn("[TenantJob] 未获取到任何启用租户，跳过执行");
            return null;
        }

        log.info("[TenantJob] 开始遍历 {} 个租户执行任务: {}", tenantIds.size(),
                joinPoint.getSignature().toShortString());

        Map<Long, String> errors = new ConcurrentHashMap<>();
        for (Long tenantId : tenantIds) {
            try {
                TenantContext.TenantInfo info = new TenantContext.TenantInfo();
                info.setTenantId(tenantId);
                TenantContext.set(info);
                joinPoint.proceed();
            } catch (Throwable e) {
                log.error("[TenantJob] 租户 {} 执行失败: {}", tenantId, e.getMessage(), e);
                errors.put(tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        if (!errors.isEmpty()) {
            log.warn("[TenantJob] 执行完毕, {} 个租户失败: {}", errors.size(), errors);
        } else {
            log.info("[TenantJob] 全部 {} 个租户执行成功", tenantIds.size());
        }
        return null;
    }
}
