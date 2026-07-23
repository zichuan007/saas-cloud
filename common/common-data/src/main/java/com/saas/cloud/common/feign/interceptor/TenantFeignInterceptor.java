package com.saas.cloud.common.feign.interceptor;

import org.springframework.stereotype.Component;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.core.security.InternalSignatureService;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

/**
 * Feign 租户/用户上下文传播拦截器
 * <p>同时注入内部调用签名（X-Internal-Signature + X-Timestamp），下游 @InnerApi 切面校验。</p>
 *
 * @author saas-cloud
 * @version V1.1
 * @since 2026-05-18
 */
@Component
@RequiredArgsConstructor
public class TenantFeignInterceptor implements RequestInterceptor {

    private final InternalSignatureService signatureService;

    @Override
    public void apply(RequestTemplate template) {
        // 内部调用签名（影子模式：下游 saas.security.signature-enforced=true 后强制校验）
        long timestamp = System.currentTimeMillis();
        String method = template.method() != null ? template.method() : "GET";
        String path = template.path() != null ? template.path() : "";
        template.header(SecurityConstants.HEADER_INTERNAL_SOURCE, "true");
        template.header(SecurityConstants.HEADER_INTERNAL_TIMESTAMP, String.valueOf(timestamp));
        template.header(SecurityConstants.HEADER_INTERNAL_SIGNATURE,
                signatureService.signInternal(method, path, timestamp));

        TenantContext.TenantInfo tenant = TenantContext.get();
        if (tenant != null && tenant.getTenantId() != null) {
            template.header(SecurityConstants.HEADER_TENANT_ID, String.valueOf(tenant.getTenantId()));
        }

        UserContext.UserInfo user = UserContext.get();
        if (user != null) {
            template.header(SecurityConstants.HEADER_USER_ID, String.valueOf(user.getUserId()));
            template.header(SecurityConstants.HEADER_USERNAME, user.getUsername());
        }
    }
}
