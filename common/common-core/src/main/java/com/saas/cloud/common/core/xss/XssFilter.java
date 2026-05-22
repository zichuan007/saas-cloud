package com.saas.cloud.common.core.xss;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局 XSS 过滤器：对 form 表单参数进行 HTML 清洗
 * JSON 请求体通过 {@link XssStringJsonDeserializer} 在 Jackson 反序列化时处理
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }
}
