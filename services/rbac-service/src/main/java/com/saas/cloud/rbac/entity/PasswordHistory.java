package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * å¯†ç åŽ†å²è¡¨
 * </p>
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
     * ç”¨æˆ·ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * åŽ†å²å¯†ç (BCrypt)
     */
    @TableField("password")
    private String password;
}
