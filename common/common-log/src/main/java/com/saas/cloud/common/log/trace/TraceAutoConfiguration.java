package com.saas.cloud.common.log.trace;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import io.micrometer.tracing.Tracer;

/**
 * 链路追踪自动配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Configuration
@ConditionalOnClass(Tracer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnBean(Tracer.class)
    public FilterRegistrationBean<TraceResponseFilter> traceResponseFilterRegistration(Tracer tracer) {
        FilterRegistrationBean<TraceResponseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceResponseFilter(tracer));
        registration.addUrlPatterns("/*");
        registration.setName("traceResponseFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        return registration;
    }
}
