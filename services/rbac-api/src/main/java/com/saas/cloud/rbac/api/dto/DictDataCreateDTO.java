package com.saas.cloud.rbac.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 字典数据创建请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
public class DictDataCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典类型编码 */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /** 字典标签 */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /** 字典键值 */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /** 排序 */
    private Integer sortOrder;

    /** 样式属性 */
    private String cssClass;

    /** 表格回显样式 */
    private String listClass;

    /** 备注 */
    private String remark;
}
