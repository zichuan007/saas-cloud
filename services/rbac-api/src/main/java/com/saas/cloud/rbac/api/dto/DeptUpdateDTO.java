package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class DeptUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long id;

    /** 部门名称 */
    private String deptName;

    /** 父部门ID */
    private Long parentId;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;
}
