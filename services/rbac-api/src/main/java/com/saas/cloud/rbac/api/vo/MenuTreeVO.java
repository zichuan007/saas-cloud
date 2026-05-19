package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class MenuTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    private Long id;

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 菜单类型（1=目录 2=菜单 3=按钮） */
    private Integer menuType;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 是否可见 */
    private Boolean visible;

    /** 子菜单 */
    private List<MenuTreeVO> children;
}
