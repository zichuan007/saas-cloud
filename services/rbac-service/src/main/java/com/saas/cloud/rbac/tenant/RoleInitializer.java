package com.saas.cloud.rbac.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializer;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.mapper.RoleMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户初始化 - 创建租户超管角色
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RoleInitializer implements TenantInitializer {

    private final RoleMapper roleMapper;

    @Override
    public String getCode() {
        return "ROLE";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void initialize(TenantInitContext context) {
        Role adminRole = new Role();
        adminRole.setRoleName("租户超管");
        adminRole.setRoleCode("tenant_admin");
        adminRole.setRoleLevel((byte) 0);
        adminRole.setDataScope((byte) 1);
        adminRole.setSortOrder(0);
        adminRole.setStatus((byte) 1);
        adminRole.setIsSystem((byte) 1);
        adminRole.setTenantId(context.getTenantId());
        roleMapper.insert(adminRole);

        context.put("adminRoleId", adminRole.getId());
        log.info("创建租户超管角色成功, roleId={}", adminRole.getId());
    }

    @Override
    public void rollback(TenantInitContext context) {
        Long roleId = context.get("adminRoleId");
        if (roleId != null) {
            roleMapper.deleteById(roleId);
            log.info("回滚租户超管角色, roleId={}", roleId);
        }
    }
}
