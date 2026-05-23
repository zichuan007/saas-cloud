package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 岗位表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_post")
public class Post extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 岗位编码 */
    @TableField("post_code")
    private String postCode;

    /** 岗位名称 */
    @TableField("post_name")
    private String postName;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    @TableField("status")
    private Integer status;
}
