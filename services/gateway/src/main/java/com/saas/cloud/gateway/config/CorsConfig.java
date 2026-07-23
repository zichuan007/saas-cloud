package com.saas.cloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;

/**
 * CORS WebFilter — 优先级最高，确保跨域头在 Sa-Token 之前写入
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-05
 */
@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsWebFilter() {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(exchange);
            }

            var response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                    request.getHeaders().getFirst(HttpHeaders.ORIGIN));
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                    "GET, POST, PUT, DELETE, OPTIONS");
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }
}
