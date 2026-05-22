package com.saas.cloud.common.sentinel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 限流/降级/授权 异常统一处理，返回 JSON 格式
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName, BlockException e) throws Exception {
        String message;
        int code = HttpStatus.TOO_MANY_REQUESTS.value();

        if (e instanceof FlowException) {
            message = "请求过于频繁，请稍后再试";
        } else if (e instanceof DegradeException) {
            message = "服务暂时不可用，请稍后再试";
            code = HttpStatus.SERVICE_UNAVAILABLE.value();
        } else if (e instanceof ParamFlowException) {
            message = "请求参数限流，请稍后再试";
        } else if (e instanceof AuthorityException) {
            message = "无权限访问";
            code = HttpStatus.FORBIDDEN.value();
        } else {
            message = "系统繁忙，请稍后再试";
        }

        log.warn("Sentinel 拦截请求: path={}, rule={}, message={}", request.getRequestURI(), e.getRule(), message);

        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);

        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
    }
}
