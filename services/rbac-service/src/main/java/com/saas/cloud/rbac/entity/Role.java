package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_role")
public class Role extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色等级 0-超管 1-管理员 2-普通
     */
    @TableField("role_level")
    private Byte roleLevel;

    /**
     * 数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义
     */
    @TableField("data_scope")
    private Byte dataScope;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 是否系统内置 0-否 1-是
     */
    @TableField("is_system")
    private Byte isSystem;
}
