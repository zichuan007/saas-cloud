package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 租户注册请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 企业名称 */
    @NotBlank(message = "企业名称不能为空")
    private String tenantName;

    /** 联系人 */
    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
