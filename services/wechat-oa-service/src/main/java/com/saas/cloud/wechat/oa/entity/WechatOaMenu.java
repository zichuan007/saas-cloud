package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 公众号菜单表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_menu")
public class WechatOaMenu extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 父菜单ID 0-一级
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单类型 click/view/miniprogram等
     */
    @TableField("menu_type")
    private String menuType;

    /**
     * 菜单KEY(click类型)
     */
    @TableField("menu_key")
    private String menuKey;

    /**
     * 菜单URL(view类型)
     */
    @TableField("menu_url")
    private String menuUrl;

    /**
     * 素材ID
     */
    @TableField("media_id")
    private String mediaId;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
