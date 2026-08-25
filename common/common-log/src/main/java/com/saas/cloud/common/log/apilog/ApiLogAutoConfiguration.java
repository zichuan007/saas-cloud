package com.saas.cloud.common.log.apilog;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;

/**
 * API 日志自动配置
 * <p>在 Web 应用且 MQ 可用时，自动注册 API 访问日志和错误日志过滤器。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(MessageSender.class)
public class ApiLogAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ApiErrorLogFilter> apiErrorLogFilterRegistration(
            MessageSender messageSender, ObjectMapper objectMapper) {
        FilterRegistrationBean<ApiErrorLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiErrorLogFilter(messageSender, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("apiErrorLogFilter");
        // 置于 TenantContextFilter(+10) 之内，异常捕获时 UserContext 尚未 clear
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 11);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilterRegistration(
            MessageSender messageSender, ObjectMapper objectMapper) {
        FilterRegistrationBean<ApiAccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiAccessLogFilter(messageSender, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("apiAccessLogFilter");
        // 置于 TenantContextFilter(+10) 之内，finally 记录时 UserContext 尚未 clear
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 12);
        return registration;
    }
}
