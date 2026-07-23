package com.saas.cloud.common.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.core.security.InternalSignatureService;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户/用户上下文过滤器：从 Gateway 注入的 Header 中提取上下文
 * <p>附带影子模式签名校验：对携带身份头的请求校验 X-Signature，
 * {@code saas.security.signature-enforced=false} 时仅告警，置 true 后 403 拒绝伪造。</p>
 *
 * @author saas-cloud
 * @version V1.1
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    private final InternalSignatureService signatureService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 影子模式验签：enforced=false 仅告警不拦截
            if (!verifySignature(request, response)) {
                return;
            }
            buildTenantContext(request);
            buildUserContext(request);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            UserContext.clear();
        }
    }

    /**
     * 校验网关身份头签名。无身份头（匿名/白名单/WebSocket 握手）直接放行。
     *
     * @return true=放行继续；false=已拒绝（强制模式）
     */
    private boolean verifySignature(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        if (StrUtil.isBlank(userId)) {
            return true;
        }
        String signature = request.getHeader(SecurityConstants.HEADER_SIGNATURE);
        String tsStr = request.getHeader(SecurityConstants.HEADER_TIMESTAMP);
        Long timestamp = null;
        if (tsStr != null) {
            try {
                timestamp = Long.parseLong(tsStr);
            } catch (NumberFormatException ignored) {
                // 留 null，验签将报缺少/非法时间戳
            }
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(SecurityConstants.HEADER_USER_ID, userId);
        headers.put(SecurityConstants.HEADER_USERNAME, request.getHeader(SecurityConstants.HEADER_USERNAME));
        headers.put(SecurityConstants.HEADER_TENANT_ID, request.getHeader(SecurityConstants.HEADER_TENANT_ID));
        headers.put(SecurityConstants.HEADER_DEPT_ID, request.getHeader(SecurityConstants.HEADER_DEPT_ID));
        headers.put(SecurityConstants.HEADER_ROLE_LEVEL, request.getHeader(SecurityConstants.HEADER_ROLE_LEVEL));
        headers.put(SecurityConstants.HEADER_DATA_SCOPE, request.getHeader(SecurityConstants.HEADER_DATA_SCOPE));
        headers.put(SecurityConstants.HEADER_PERMISSIONS, request.getHeader(SecurityConstants.HEADER_PERMISSIONS));

        InternalSignatureService.VerifyResult result =
                signatureService.verifyUserHeaders(headers, signature, timestamp);
        if (result.isOk()) {
            return true;
        }
        if (signatureService.isEnforced()) {
            log.warn("[签名校验] 拒绝请求(强制模式): uri={}, reason={}", request.getRequestURI(), result.getReason());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"请求签名校验失败\",\"data\":null}");
            return false;
        }
        log.warn("[签名校验] 影子模式告警(不拦截): uri={}, reason={}", request.getRequestURI(), result.getReason());
        return true;
    }

    private void buildTenantContext(HttpServletRequest request) {
        String tenantId = request.getHeader(SecurityConstants.HEADER_TENANT_ID);
        if (StrUtil.isNotBlank(tenantId)) {
            TenantContext.TenantInfo info = new TenantContext.TenantInfo();
            info.setTenantId(Long.valueOf(tenantId));
            TenantContext.set(info);
        }
    }

    private void buildUserContext(HttpServletRequest request) {
        String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        if (StrUtil.isBlank(userId)) {
            return;
        }
        UserContext.UserInfo user = new UserContext.UserInfo();
        user.setUserId(Long.valueOf(userId));
        user.setUsername(request.getHeader(SecurityConstants.HEADER_USERNAME));

        String tenantId = request.getHeader(SecurityConstants.HEADER_TENANT_ID);
        if (StrUtil.isNotBlank(tenantId)) {
            user.setTenantId(Long.valueOf(tenantId));
        }

        String deptId = request.getHeader(SecurityConstants.HEADER_DEPT_ID);
        if (StrUtil.isNotBlank(deptId)) {
            user.setDeptId(Long.valueOf(deptId));
        }

        String roleLevel = request.getHeader(SecurityConstants.HEADER_ROLE_LEVEL);
        if (StrUtil.isNotBlank(roleLevel)) {
            user.setRoleLevel(Integer.valueOf(roleLevel));
        }

        String dataScope = request.getHeader(SecurityConstants.HEADER_DATA_SCOPE);
        if (StrUtil.isNotBlank(dataScope)) {
            user.setDataScope(Integer.valueOf(dataScope));
        }

        String permissions = request.getHeader(SecurityConstants.HEADER_PERMISSIONS);
        if (StrUtil.isNotBlank(permissions)) {
            Set<String> permSet = new HashSet<>(Arrays.asList(permissions.split(",")));
            user.setPermissions(permSet);
        } else {
            user.setPermissions(Collections.emptySet());
        }

        UserContext.set(user);
    }
}
