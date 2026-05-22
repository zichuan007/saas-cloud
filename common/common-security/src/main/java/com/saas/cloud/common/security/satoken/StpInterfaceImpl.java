package com.saas.cloud.common.security.satoken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.saas.cloud.common.security.context.UserContext;

import cn.dev33.satoken.stp.StpInterface;

/**
 * Sa-Token 权限/角色认证接口实现，从 UserContext 中读取
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserContext.UserInfo userInfo = UserContext.get();
        if (userInfo == null) {
            return Collections.emptyList();
        }
        Set<String> permissions = userInfo.getPermissions();
        return permissions != null ? new ArrayList<>(permissions) : Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
