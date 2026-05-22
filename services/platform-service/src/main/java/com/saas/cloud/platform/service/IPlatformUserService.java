package com.saas.cloud.platform.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.platform.api.vo.PlatformUserVO;
import com.saas.cloud.platform.entity.PlatformUser;

/**
 * 平台用户服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IPlatformUserService extends IService<PlatformUser> {

    /**
     * 平台管理员登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 包含 accessToken、refreshToken 等信息的 Map
     */
    Map<String, Object> platformLogin(String username, String password);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 平台用户视图对象
     */
    PlatformUserVO getCurrentUser(Long userId);
}
