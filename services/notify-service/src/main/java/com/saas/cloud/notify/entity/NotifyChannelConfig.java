package com.saas.cloud.notify.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 租户通知渠道配置表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("notify_channel_config")
public class NotifyChannelConfig extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 渠道 0-站内信 1-邮件 2-飞书 3-钉钉 4-企业微信
     */
    @TableField("channel_type")
    private Byte channelType;

    /**
     * 是否启用 0-否 1-是
     */
    @TableField("enabled")
    private Byte enabled;

    /**
     * 渠道配置(JSON)
     */
    @TableField("config_json")
    private String configJson;
}
