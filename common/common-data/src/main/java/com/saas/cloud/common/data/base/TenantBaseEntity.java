package com.saas.cloud.common.data.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户实体基类，在 BaseEntity 基础上增加 tenant_id
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantBaseEntity extends BaseEntity {

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
