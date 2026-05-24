package com.saas.cloud.common.log.apilog;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.kafka.producer.KafkaProducerService;
import com.saas.cloud.common.security.context.UserContext;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API 访问日志过滤器
 * <p>记录每个 HTTP 请求的 URL、耗时、响应码等信息，通过 Kafka 异步发送。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApiAccessLogFilter extends OncePerRequestFilter {

    private final KafkaProducerService kafkaProducerService;

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            try {
                recordAccessLog(request, response, duration);
            } catch (Exception e) {
                log.warn("[API访问日志] 记录失败: {}", e.getMessage());
            }
        }
    }

    private void recordAccessLog(HttpServletRequest request, HttpServletResponse response, long duration) {
        ApiAccessLogEvent event = new ApiAccessLogEvent();
        event.setRequestUrl(request.getRequestURI());
        event.setRequestMethod(request.getMethod());
        event.setQueryString(truncate(request.getQueryString(), 500));
        event.setIp(getClientIp(request));
        event.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        event.setHttpStatus(response.getStatus());
        event.setDuration(duration);
        event.setRequestTime(System.currentTimeMillis() - duration);

        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                event.setTraceId(currentSpan.context().traceId());
            }
        }

        UserContext.UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            event.setUserId(userInfo.getUserId());
            event.setUsername(userInfo.getUsername());
            event.setTenantId(userInfo.getTenantId());
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaProducerService.send(KafkaConfig.TOPIC_API_ACCESS_LOG, json);
        } catch (Exception e) {
            log.debug("[API访问日志] Kafka 发送失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.endsWith(".css") || uri.endsWith(".js")
                || uri.endsWith(".ico") || uri.endsWith(".png") || uri.endsWith(".jpg");
    }
}
