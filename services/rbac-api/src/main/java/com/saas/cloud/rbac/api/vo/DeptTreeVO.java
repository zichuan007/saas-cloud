package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 部门树VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class DeptTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long id;

    /** 父部门ID */
    private Long parentId;

    /** 部门名称 */
    private String deptName;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 子部门 */
    private List<DeptTreeVO> children;
}
