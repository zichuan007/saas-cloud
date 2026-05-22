package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class RoleCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 角色标识 */
    private String roleCode;

    /** 排序 */
    private Integer sortOrder;

    /** 数据范围（1-5） */
    private Integer dataScope;

    /** 菜单ID列表 */
    private List<Long> menuIds;

    /** 部门ID列表（自定义数据范围时） */
    private List<Long> deptIds;
}
