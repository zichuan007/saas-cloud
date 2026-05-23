package com.saas.cloud.rbac.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 岗位 修改参数
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class PostUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 岗位ID */
    @NotNull(message = "岗位ID不能为空")
    private Long id;

    /** 岗位编码 */
    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码长度不能超过64")
    private String postCode;

    /** 岗位名称 */
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 128, message = "岗位名称长度不能超过128")
    private String postName;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
