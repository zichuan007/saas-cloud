package com.saas.cloud.rbac.tenant;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializer;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户初始化 - 创建管理员用户
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminUserInitializer implements TenantInitializer {

    private final UserMapper userMapper;

    @Override
    public String getCode() {

        return "ADMIN_USER";
    }

    @Override
    public int getOrder() {

        return 30;
    }

    @Override
    public void initialize(TenantInitContext context) {

        Long rootDeptId = context.get("rootDeptId");

        User adminUser = new User();
        adminUser.setUsername(context.getContactPhone());
        adminUser.setPassword(context.getPassword());
        adminUser.setRealName(context.getContactPerson());
        adminUser.setPhone(context.getContactPhone());
        adminUser.setDeptId(rootDeptId);
        adminUser.setStatus((byte) 1);
        adminUser.setRoleLevel((byte) 0);
        adminUser.setPasswordUpdateTime(LocalDateTime.now());
        adminUser.setTenantId(context.getTenantId());
        userMapper.insert(adminUser);

        context.put("adminUserId", adminUser.getId());
        log.info("创建管理员用户成功, userId={}", adminUser.getId());
    }

    @Override
    public void rollback(TenantInitContext context) {

        Long userId = context.get("adminUserId");
        if (userId != null) {
            userMapper.deleteById(userId);
            log.info("回滚管理员用户, userId={}", userId);
        }
    }

}
