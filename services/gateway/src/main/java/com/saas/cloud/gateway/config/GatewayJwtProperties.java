package com.saas.cloud.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway JWT 配置属性
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "saas.jwt")
public class GatewayJwtProperties {

    /**
     * JWT 签名密钥
     */
    private String secret = "saas-cloud-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256";
}
