package com.saas.cloud.common.feign.interceptor;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Feign 租户/用户上下文传播拦截器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Component
public class TenantFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 注入内部调用标识，配合 @InnerApi 切面校验
        template.header(SecurityConstants.HEADER_INTERNAL_SOURCE, "true");

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
