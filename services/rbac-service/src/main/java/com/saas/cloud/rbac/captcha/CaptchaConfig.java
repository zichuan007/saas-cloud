package com.saas.cloud.rbac.captcha;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;

/**
 * AJ-Captcha 配置：指定 Redis 缓存策略，手动注册 CaptchaService（兼容 Spring Boot 3）
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

    @Bean
    public CaptchaService captchaService(Environment env) {
        Properties props = new Properties();
        props.setProperty("captcha.cacheType", env.getProperty("aj.captcha.cache-type", "redis"));
        props.setProperty("captcha.type", env.getProperty("aj.captcha.type", "blockPuzzle"));
        props.setProperty("captcha.water.mark", env.getProperty("aj.captcha.water-mark", "saas-cloud"));
        props.setProperty("captcha.interference.options", env.getProperty("aj.captcha.interference-options", "0"));
        props.setProperty("captcha.jigsaw", env.getProperty("aj.captcha.jigsaw", "classpath:images/jigsaw"));
        props.setProperty("captcha.pic.click", env.getProperty("aj.captcha.pic-click", "classpath:images/pic-click"));
        return CaptchaServiceFactory.getInstance(props);
    }
}
