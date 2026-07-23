package com.saas.cloud.rbac.service;

import java.util.List;
import java.util.Map;

import com.saas.cloud.rbac.api.dto.RegisterDTO;
import com.saas.cloud.rbac.api.vo.RegisterVO;

/**
 * 认证服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IAuthService {

    Map<String, Object> login(String username, String password, String tenantCode,
                              String captchaVerification);

    Map<String, Object> refreshToken(String refreshToken);

    void logout(String token);

    /**
     * 租户注册：创建租户 + 管理员用户 + 默认角色 + 根部门，返回Token直接登录
     *
     * @param dto 注册请求
     * @return 注册结果（含Token）
     */
    RegisterVO register(RegisterDTO dto);

    /**
     * 获取用户信息（供 user-info 接口使用）
     *
     * @param userId 用户ID
     * @return 用户信息 Map
     */
    Map<String, Object> getUserInfo(Long userId);

    /**
     * 获取用户权限码列表
     *
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> getPermissionCodes(Long userId);
}
