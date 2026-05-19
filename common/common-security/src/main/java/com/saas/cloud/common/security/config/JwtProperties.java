package com.saas.cloud.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "saas.jwt")
public class JwtProperties {

    private String secret = "saas-cloud-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256";

    private long accessTokenExpire = 7200;

    private long refreshTokenExpire = 604800;

    private String issuer = "saas-cloud";
}
