package com.saas.cloud.platform.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 平台菜单树VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
@Data
public class PlatformMenuTreeVO implements Serializable {

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

    /** 菜单类型 0-目录 1-菜单 2-按钮 */
    private Integer menuType;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 是否可见 */
    private Boolean visible;

    /** 子菜单 */
    private List<PlatformMenuTreeVO> children;
}
