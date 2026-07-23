package com.saas.cloud.common.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内部调用签名自动配置
 * <p>装配 {@link InternalSignatureService}，供网关签名与下游验签共用。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-07-22
 */
@Configuration
public class SignatureAutoConfiguration {

    @Bean
    public InternalSignatureService internalSignatureService(
            @Value("${saas.security.internal-secret:saas-cloud-internal-secret-change-me}") String secret,
            @Value("${saas.security.signature-enforced:false}") boolean enforced,
            @Value("${saas.security.signature-max-age-seconds:300}") long maxAgeSeconds) {
        return new InternalSignatureService(secret, enforced, maxAgeSeconds);
    }
}
