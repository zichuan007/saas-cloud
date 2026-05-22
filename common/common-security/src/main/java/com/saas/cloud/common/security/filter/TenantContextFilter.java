package com.saas.cloud.common.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saas.cloud.common.core.constant.SecurityConstants;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户/用户上下文过滤器：从 Gateway 注入的 Header 中提取上下文
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            buildTenantContext(request);
            buildUserContext(request);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            UserContext.clear();
        }
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
