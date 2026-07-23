package com.saas.cloud.common.security.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.core.exception.ForbiddenException;
import com.saas.cloud.common.core.security.InternalSignatureService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 内部接口访问控制切面
 * <p>拦截标注了 {@link com.saas.cloud.common.security.annotation.InnerApi} 的类或方法，
 * 校验请求头 X-Internal-Source 并验证 X-Internal-Signature（HMAC，防伪造）。
 * 影子模式：signature-enforced=false 时签名失败仅告警，置 true 后 403。</p>
 *
 * @author saas-cloud
 * @version V1.1
 * @since 2026-05-20
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class InnerApiAspect {

    private final InternalSignatureService signatureService;

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

        // 内部调用签名校验（影子模式）
        String signature = request.getHeader(SecurityConstants.HEADER_INTERNAL_SIGNATURE);
        String tsStr = request.getHeader(SecurityConstants.HEADER_INTERNAL_TIMESTAMP);
        Long timestamp = null;
        if (tsStr != null) {
            try {
                timestamp = Long.parseLong(tsStr);
            } catch (NumberFormatException ignored) {
                // 留 null，验签报缺少时间戳
            }
        }
        InternalSignatureService.VerifyResult result = signatureService.verifyInternal(
                request.getMethod(), request.getRequestURI(), signature, timestamp);
        if (!result.isOk()) {
            String msg = "uri=" + request.getRequestURI() + ", reason=" + result.getReason();
            if (signatureService.isEnforced()) {
                log.warn("[内部接口] 签名校验失败(拒绝): {}", msg);
                throw new ForbiddenException("内部调用签名校验失败");
            }
            log.warn("[内部接口] 签名校验失败(影子模式告警): {}", msg);
        }

        return pjp.proceed();
    }
}
