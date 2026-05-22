package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据字典类型表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("sys_dict_type")
public class DictType extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 字典名称 */
    @TableField("dict_name")
    private String dictName;

    /** 字典类型编码 */
    @TableField("dict_type")
    private String dictType;

    /** 状态 0-禁用 1-启用 */
    @TableField("status")
    private Byte status;
}
