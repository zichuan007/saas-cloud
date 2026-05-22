package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据视图
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
public class DictDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 字典类型编码 */
    private String dictType;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 样式属性 */
    private String cssClass;

    /** 表格回显样式 */
    private String listClass;

    /** 备注 */
    private String remark;
}
