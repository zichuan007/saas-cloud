package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * è§’è‰²èœå•å…³è”è¡¨
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_role_menu")
public class RoleMenu extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * è§’è‰²ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * èœå•ID
     */
    @TableField("menu_id")
    private Long menuId;
}
