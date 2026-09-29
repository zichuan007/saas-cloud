package com.saas.cloud.common.feign.advice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 调用异常精细化处理
 * <p>解析被调方 ApiResult 响应体的 message 字段，避免暴露 Feign 默认的
 * {@code [500 ...] during [POST] to [url]} 内部串。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-09-29
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(FeignException.class)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FeignExceptionAdvice {

    private final ObjectMapper objectMapper;

    /**
     * 处理 Feign 调用异常
     *
     * @param e FeignException
     * @return 响应体
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResult<Void>> handleFeignException(FeignException e) {
        int status = e.status();
        String message = extractDownstreamMessage(e);
        log.warn("Feign 调用异常 status={} msg={}", status, message, e);
        // 4xx 业务错误：透传下游业务 message
        if (status >= 400 && status < 500) {
            return ResponseEntity.status(status)
                    .body(ApiResult.fail(status, message));
        }
        // 5xx 服务端错误：脱敏，避免泄露下游堆栈细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(ResultCode.INTERNAL_ERROR.getCode(), "服务调用失败，请稍后再试"));
    }

    /**
     * 从 FeignException 响应体解析被调方 ApiResult 的 message 字段
     *
     * @param e FeignException
     * @return 下游业务消息，解析失败回退 e.getMessage()
     */
    private String extractDownstreamMessage(FeignException e) {
        try {
            byte[] body = e.content();
            if (body == null || body.length == 0) {
                return e.getMessage();
            }
            JsonNode node = objectMapper.readTree(body);
            JsonNode msg = node.get("message");
            if (msg != null && !msg.isNull()) {
                return msg.asText();
            }
            return e.getMessage();
        } catch (Exception ex) {
            return e.getMessage();
        }
    }
}
