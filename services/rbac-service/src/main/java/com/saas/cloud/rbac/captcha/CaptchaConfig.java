package com.saas.cloud.rbac.captcha;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.anji.captcha.service.CaptchaCacheService;

/**
 * AJ-Captcha 配置：指定 Redis 缓存策略
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Configuration
public class CaptchaConfig {

    @Bean
    @Primary
    public Map<String, CaptchaCacheService> captchaCacheServiceMap(CaptchaRedisService captchaRedisService) {
        Map<String, CaptchaCacheService> map = new HashMap<>(4);
        map.put(captchaRedisService.type(), captchaRedisService);
        return map;
    }
}
