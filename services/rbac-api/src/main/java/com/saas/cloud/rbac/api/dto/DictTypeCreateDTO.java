package com.saas.cloud.rbac.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典类型创建请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
public class DictTypeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典名称 */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /** 字典类型编码 */
    @NotBlank(message = "字典类型编码不能为空")
    private String dictType;

    /** 备注 */
    private String remark;
}
