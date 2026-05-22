package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据字典数据表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("sys_dict_data")
public class DictData extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 字典类型编码 */
    @TableField("dict_type")
    private String dictType;

    /** 字典标签 */
    @TableField("dict_label")
    private String dictLabel;

    /** 字典键值 */
    @TableField("dict_value")
    private String dictValue;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    @TableField("status")
    private Byte status;

    /** 样式属性 */
    @TableField("css_class")
    private String cssClass;

    /** 表格回显样式 */
    @TableField("list_class")
    private String listClass;
}
