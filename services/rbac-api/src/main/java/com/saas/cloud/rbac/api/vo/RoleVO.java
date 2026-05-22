package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 角色列表视图VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 角色等级 0-超管 1-管理员 2-普通 */
    private Integer roleLevel;

    /** 数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义 */
    private Integer dataScope;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 关联的菜单ID列表 */
    private List<Long> menuIds;
}
