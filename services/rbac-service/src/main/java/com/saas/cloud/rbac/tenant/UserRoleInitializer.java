package com.saas.cloud.rbac.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializer;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户初始化 - 关联用户角色
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserRoleInitializer implements TenantInitializer {

    private final UserRoleMapper userRoleMapper;

    @Override
    public String getCode() {
        return "USER_ROLE";
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public void initialize(TenantInitContext context) {
        Long adminUserId = context.get("adminUserId");
        Long adminRoleId = context.get("adminRoleId");

        UserRole userRole = new UserRole();
        userRole.setUserId(adminUserId);
        userRole.setRoleId(adminRoleId);
        userRole.setTenantId(context.getTenantId());
        userRoleMapper.insert(userRole);

        context.put("userRoleId", userRole.getId());
        log.info("关联用户角色成功, userId={}, roleId={}", adminUserId, adminRoleId);
    }

    @Override
    public void rollback(TenantInitContext context) {
        Long userRoleId = context.get("userRoleId");
        if (userRoleId != null) {
            userRoleMapper.deleteById(userRoleId);
            log.info("回滚用户角色关联, userRoleId={}", userRoleId);
        }
    }
}
