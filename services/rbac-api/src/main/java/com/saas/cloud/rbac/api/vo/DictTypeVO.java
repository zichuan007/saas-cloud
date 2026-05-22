package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型视图
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
public class DictTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 字典名称 */
    private String dictName;

    /** 字典类型编码 */
    private String dictType;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
