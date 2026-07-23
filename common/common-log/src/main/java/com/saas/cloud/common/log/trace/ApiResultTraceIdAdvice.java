package com.saas.cloud.common.log.trace;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.saas.cloud.common.core.result.ApiResult;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

/**
 * 响应体 traceId 回填
 * <p>在 {@link ApiResult} 序列化前注入当前链路 traceId，
 * 使前端拿到响应即可关联后端日志，无需依赖响应头。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-07-22
 */
@Slf4j
public class ApiResultTraceIdAdvice implements ResponseBodyAdvice<Object> {

    private final Tracer tracer;

    public ApiResultTraceIdAdvice(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                             Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResult) {
            String traceId = null;
            if (tracer != null && tracer.currentSpan() != null) {
                traceId = tracer.currentSpan().context().traceId();
            }
            ((ApiResult<?>) body).setTraceId(traceId);
        }
        return body;
    }
}
