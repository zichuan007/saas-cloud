package com.saas.cloud.rbac.service;

import com.saas.cloud.rbac.api.vo.OnlineUserVO;

import java.util.List;

/**
 * 在线用户 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface IOnlineUserService {

    /**
     * 查询当前租户在线用户列表
     *
     * @param username 用户名筛选（可选）
     * @return 在线用户列表
     */
    List<OnlineUserVO> listOnlineUsers(String username);

    /**
     * 强制下线指定用户
     *
     * @param tokenValue token值
     */
    void kickout(String tokenValue);
}
