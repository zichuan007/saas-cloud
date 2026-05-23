package com.saas.cloud.rbac.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 社交登录绑定表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_social_user")
public class SocialUser extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联的系统用户ID */
    @TableField("user_id")
    private Long userId;

    /** 平台类型 wechat/dingtalk/github/gitee */
    @TableField("social_type")
    private String socialType;

    /** 第三方平台用户ID */
    @TableField("social_id")
    private String socialId;

    /** 第三方平台用户名 */
    @TableField("social_name")
    private String socialName;

    /** 头像 */
    @TableField("social_avatar")
    private String socialAvatar;

    /** 访问令牌 */
    @TableField("access_token")
    private String accessToken;

    /** 刷新令牌 */
    @TableField("refresh_token")
    private String refreshToken;

    /** 令牌过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
