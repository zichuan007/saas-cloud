package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * ç”¨æˆ·è§’è‰²å…³è”è¡¨
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_user_role")
public class UserRole extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ç”¨æˆ·ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * è§’è‰²ID
     */
    @TableField("role_id")
    private Long roleId;
}
