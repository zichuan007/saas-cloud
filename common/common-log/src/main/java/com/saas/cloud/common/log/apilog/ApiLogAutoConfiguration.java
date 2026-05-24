package com.saas.cloud.common.log.apilog;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.kafka.producer.KafkaProducerService;

/**
 * API 日志自动配置
 * <p>在 Web 应用且 Kafka 可用时，自动注册 API 访问日志和错误日志过滤器。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(KafkaProducerService.class)
public class ApiLogAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ApiErrorLogFilter> apiErrorLogFilterRegistration(
            KafkaProducerService kafkaProducerService, ObjectMapper objectMapper) {
        FilterRegistrationBean<ApiErrorLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiErrorLogFilter(kafkaProducerService, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("apiErrorLogFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilterRegistration(
            KafkaProducerService kafkaProducerService, ObjectMapper objectMapper) {
        FilterRegistrationBean<ApiAccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiAccessLogFilter(kafkaProducerService, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("apiAccessLogFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }
}
