package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 粉丝标签表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_user_tag")
public class WechatOaUserTag extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 微信端标签ID
     */
    @TableField("wx_tag_id")
    private Integer wxTagId;

    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 粉丝数
     */
    @TableField("fan_count")
    private Integer fanCount;
}
