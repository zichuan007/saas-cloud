package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门创建请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class DeptCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门名称 */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    /** 父部门ID，默认0表示顶级 */
    private Long parentId;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 排序 */
    private Integer sortOrder;
}
