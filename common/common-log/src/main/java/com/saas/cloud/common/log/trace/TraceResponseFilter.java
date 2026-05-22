package com.saas.cloud.common.log.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 将当前请求的 traceId 写入 HTTP 响应头，便于前端或调用方关联日志
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TraceResponseFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.setHeader(TRACE_ID_HEADER, currentSpan.context().traceId());
        }
        filterChain.doFilter(request, response);
    }
}
