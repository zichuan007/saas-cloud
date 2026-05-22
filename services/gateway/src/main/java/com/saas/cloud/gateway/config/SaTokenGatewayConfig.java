package com.saas.cloud.gateway.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token Gateway 全局过滤器配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SaTokenGatewayConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SaReactorFilter getSaReactorFilter() {

        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(
                        "/api/rbac/auth/login",
                        "/api/rbac/auth/refresh",
                        "/api/rbac/auth/register",
                        "/api/rbac/captcha/**",
                        "/api/platform/auth/login",
                        "/api/generator/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/favicon.ico"
                )
                .setAuth(obj -> {
                    SaRouter.match("/**", r -> StpUtil.checkLogin());
                })
                .setError(e -> {
                    log.warn("Sa-Token 认证失败: {}", e.getMessage());
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("code", HttpStatus.UNAUTHORIZED.value());
                    result.put("message", "Token无效或已过期");
                    result.put("data", null);
                    try {
                        return objectMapper.writeValueAsString(result);
                    } catch (Exception ex) {
                        return "{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}";
                    }
                });
    }

}
