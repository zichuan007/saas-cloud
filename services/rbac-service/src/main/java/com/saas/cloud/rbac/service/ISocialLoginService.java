package com.saas.cloud.rbac.service;

import java.util.List;
import java.util.Map;

import com.saas.cloud.rbac.entity.SocialUser;

/**
 * 社交登录服务
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface ISocialLoginService {

    /**
     * 获取第三方平台授权URL
     *
     * @param type      平台类型（wechat/dingtalk/github/gitee）
     * @param tenantCode 租户编码
     * @return 授权URL
     */
    String getAuthorizeUrl(String type, String tenantCode);

    /**
     * 第三方登录回调处理
     *
     * @param type      平台类型
     * @param tenantCode 租户编码
     * @param code      授权码
     * @param state     状态参数
     * @return 登录结果（已绑定返回 token，未绑定返回 socialId 等信息供绑定）
     */
    Map<String, Object> callback(String type, String tenantCode, String code, String state);

    /**
     * 绑定社交账号到当前用户
     *
     * @param userId     当前用户ID
     * @param socialType 平台类型
     * @param socialId   第三方用户ID
     * @param socialName 第三方用户名
     * @param socialAvatar 头像
     */
    void bindSocial(Long userId, String socialType, String socialId, String socialName, String socialAvatar);

    /**
     * 解绑社交账号
     *
     * @param userId     当前用户ID
     * @param socialType 平台类型
     */
    void unbindSocial(Long userId, String socialType);

    /**
     * 查询用户已绑定的社交账号
     *
     * @param userId 用户ID
     * @return 已绑定的社交账号列表
     */
    List<SocialUser> listBoundSocials(Long userId);
}
