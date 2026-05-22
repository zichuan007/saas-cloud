package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 部门表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_dept")
public class Dept extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 父部门ID 0-顶级
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 祖先链，逗号分隔
     */
    @TableField("ancestors")
    private String ancestors;

    /**
     * 部门负责人ID
     */
    @TableField("leader_user_id")
    private Long leaderUserId;

    /**
     * 负责人姓名
     */
    @TableField("leader")
    private String leader;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

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

}
