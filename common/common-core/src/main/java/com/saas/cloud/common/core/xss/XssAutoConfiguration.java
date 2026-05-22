package com.saas.cloud.common.core.xss;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * XSS 防护自动配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ObjectMapper.class)
public class XssAutoConfiguration {

    /**
     * 注册 XSS 过滤器（处理 form 表单参数）
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 注册 Jackson XSS 反序列化模块（处理 JSON 请求体）
     */
    @Bean
    public SimpleModule xssJacksonModule() {
        SimpleModule module = new SimpleModule("XssStringModule");
        module.addDeserializer(String.class, new XssStringJsonDeserializer());
        return module;
    }
}
