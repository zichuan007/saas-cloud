package com.saas.cloud.common.security.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.core.exception.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 内部接口访问控制切面
 * 拦截标注了 {@link com.saas.cloud.common.security.annotation.InnerApi} 的类或方法，
 * 校验请求头中是否携带内部调用标识 {@code X-Internal-Source: true}，
 * 未携带则拒绝访问返回 403。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Aspect
@Component
public class InnerApiAspect {

    @Around("@within(com.saas.cloud.common.security.annotation.InnerApi) || " +
            "@annotation(com.saas.cloud.common.security.annotation.InnerApi)")
    public Object check(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new ForbiddenException("非法访问内部接口");
        }

        HttpServletRequest request = attributes.getRequest();
        String internalSource = request.getHeader(SecurityConstants.HEADER_INTERNAL_SOURCE);
        if (!"true".equals(internalSource)) {
            log.warn("外部请求访问内部接口被拦截, uri={}, remoteAddr={}",
                    request.getRequestURI(), request.getRemoteAddr());
            throw new ForbiddenException("该接口仅供内部服务调用");
        }

        return pjp.proceed();
    }
}
