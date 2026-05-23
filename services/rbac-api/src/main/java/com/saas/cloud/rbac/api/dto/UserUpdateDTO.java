package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 部门ID */
    private Long deptId;

    /** 角色ID列表 */
    private List<Long> roleIds;

    /** 岗位ID列表 */
    private List<Long> postIds;
}
