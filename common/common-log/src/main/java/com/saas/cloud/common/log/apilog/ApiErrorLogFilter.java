package com.saas.cloud.common.log.apilog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
 * API 错误日志过滤器
 * <p>捕获请求处理过程中抛出的异常，记录异常堆栈信息，通过 Kafka 异步发送。
 * 异常会被重新抛出，不影响全局异常处理器的正常工作。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApiErrorLogFilter extends OncePerRequestFilter {

    /** 异常堆栈最大保留长度 */
    private static final int MAX_STACK_TRACE_LENGTH = 4000;

    private final KafkaProducerService kafkaProducerService;

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            recordErrorLog(request, e);
            throw e;
        }
    }

    private void recordErrorLog(HttpServletRequest request, Exception ex) {
        try {
            ApiErrorLogEvent event = new ApiErrorLogEvent();
            event.setRequestUrl(request.getRequestURI());
            event.setRequestMethod(request.getMethod());
            event.setQueryString(truncate(request.getQueryString(), 500));
            event.setIp(getClientIp(request));
            event.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
            event.setTimestamp(System.currentTimeMillis());

            event.setExceptionName(ex.getClass().getName());
            event.setExceptionMessage(truncate(ex.getMessage(), 500));
            event.setExceptionStackTrace(getStackTrace(ex));

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

            String json = objectMapper.writeValueAsString(event);
            kafkaProducerService.send(KafkaConfig.TOPIC_API_ERROR_LOG, json);
        } catch (Exception e) {
            log.warn("[API错误日志] 记录失败: {}", e.getMessage());
        }
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        if (stackTrace.length() > MAX_STACK_TRACE_LENGTH) {
            return stackTrace.substring(0, MAX_STACK_TRACE_LENGTH) + "...(truncated)";
        }
        return stackTrace;
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
        return uri.startsWith("/actuator");
    }
}
