package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 菜单表（平台级）
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_menu")
public class Menu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 父菜单ID 0-顶级
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 类型 0-目录 1-菜单 2-按钮
     */
    @TableField("menu_type")
    private Byte menuType;

    /**
     * 路由路径
     */
    @TableField("path")
    private String path;

    /**
     * 组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 权限标识
     */
    @TableField("permission")
    private String permission;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 是否可见 0-隐藏 1-显示
     */
    @TableField("visible")
    private Byte visible;

    /**
     * 是否外链
     */
    @TableField("is_external")
    private Byte isExternal;

    /**
     * 是否缓存
     */
    @TableField("is_cached")
    private Byte isCached;

    /**
     * 所属模块 RBAC/WORKFLOW/WECHAT_OA
     */
    @TableField("module")
    private String module;
}
