package com.saas.cloud.rbac.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 菜单更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class MenuUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @NotNull(message = "菜单ID不能为空")
    private Long id;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /** 父菜单ID */
    private Long parentId;

    /** 类型 0-目录 1-菜单 2-按钮 */
    @NotNull(message = "菜单类型不能为空")
    private Byte menuType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 是否可见 0-隐藏 1-显示 */
    private Byte visible;

    /** 是否外链 */
    private Byte isExternal;

    /** 是否缓存 */
    private Byte isCached;

    /** 所属模块 */
    private String module;
}
