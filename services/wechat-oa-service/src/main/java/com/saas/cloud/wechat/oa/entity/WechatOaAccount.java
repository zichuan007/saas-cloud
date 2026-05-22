package com.saas.cloud.wechat.oa.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 公众号账号表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_account")
public class WechatOaAccount extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号名称
     */
    @TableField("account_name")
    private String accountName;

    /**
     * 微信AppID
     */
    @TableField("app_id")
    private String appId;

    /**
     * 微信AppSecret(加密存储)
     */
    @TableField("app_secret")
    private String appSecret;

    /**
     * 微信Token
     */
    @TableField("token")
    private String token;

    /**
     * 消息加密密钥
     */
    @TableField("aes_key")
    private String aesKey;

    /**
     * 类型 0-订阅号 1-服务号
     */
    @TableField("account_type")
    private Byte accountType;

    /**
     * 是否认证 0-否 1-是
     */
    @TableField("is_verified")
    private Byte isVerified;

    /**
     * 公众号二维码URL
     */
    @TableField("qr_code_url")
    private String qrCodeUrl;

    /**
     * 当前AccessToken(加密存储)
     */
    @TableField("access_token")
    private String accessToken;

    /**
     * AccessToken过期时间
     */
    @TableField("access_token_expire_time")
    private LocalDateTime accessTokenExpireTime;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;
}
