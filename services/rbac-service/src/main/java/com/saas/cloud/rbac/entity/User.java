package com.saas.cloud.rbac.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_user")
public class User extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码(BCrypt)
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别 0-未知 1-男 2-女
     */
    @TableField("gender")
    private Byte gender;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 角色等级 0-租户超管 1-部门主管 2-普通
     */
    @TableField("role_level")
    private Byte roleLevel;

    /**
     * 邀请码
     */
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 邀请状态 0-待接受 1-已接受 2-已过期
     */
    @TableField("invite_status")
    private Byte inviteStatus;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后修改密码时间
     */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;
}
