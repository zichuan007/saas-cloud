package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户列表分页视图VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class UserPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 角色等级 */
    private Integer roleLevel;

    /** 创建时间 */
    private LocalDateTime createTime;
}
