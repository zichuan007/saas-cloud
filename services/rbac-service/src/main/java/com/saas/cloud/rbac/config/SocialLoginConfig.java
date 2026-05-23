package com.saas.cloud.rbac.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 社交登录配置
 * <p>从 yml 读取各平台 clientId/clientSecret/redirectUri</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "social.login")
public class SocialLoginConfig {

    /** 各平台配置，key 为平台类型（wechat/dingtalk/github/gitee） */
    private Map<String, PlatformConfig> platforms = new HashMap<>();

    @Data
    public static class PlatformConfig {
        /** 客户端ID */
        private String clientId;
        /** 客户端密钥 */
        private String clientSecret;
        /** 回调地址 */
        private String redirectUri;
    }
}
