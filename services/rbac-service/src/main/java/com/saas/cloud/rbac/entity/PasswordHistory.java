package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 密码历史表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_password_history")
public class PasswordHistory extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 历史密码(BCrypt)
     */
    @TableField("password")
    private String password;
}
