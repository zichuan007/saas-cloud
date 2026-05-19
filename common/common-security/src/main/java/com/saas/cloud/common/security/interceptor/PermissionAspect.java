package com.saas.cloud.common.security.interceptor;

import com.saas.cloud.common.core.exception.ForbiddenException;
import com.saas.cloud.common.security.annotation.RequirePermission;
import com.saas.cloud.common.security.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(perm)")
    public Object check(ProceedingJoinPoint pjp, RequirePermission perm) throws Throwable {
        UserContext.UserInfo user = UserContext.get();
        if (user == null) {
            throw new ForbiddenException("未获取到用户信息");
        }
        if (user.getRoleLevel() != null && user.getRoleLevel() == 0) {
            return pjp.proceed();
        }
        if (user.getPermissions() == null || !user.getPermissions().contains(perm.value())) {
            throw new ForbiddenException("无权限: " + perm.value());
        }
        return pjp.proceed();
    }
}
