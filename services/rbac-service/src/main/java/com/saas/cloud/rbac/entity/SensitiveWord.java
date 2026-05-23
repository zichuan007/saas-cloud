package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 敏感词表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_sensitive_word")
public class SensitiveWord extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 敏感词 */
    @TableField("word")
    private String word;

    /** 分类（涉政/色情/暴力/广告等） */
    @TableField("category")
    private String category;

    /** 状态 0-禁用 1-启用 */
    @TableField("status")
    private Byte status;
}
