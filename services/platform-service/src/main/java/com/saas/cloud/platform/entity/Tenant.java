package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 租户表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_tenant")
public class Tenant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 租户名称
     */
    @TableField("tenant_name")
    private String tenantName;

    /**
     * 租户编码
     */
    @TableField("tenant_code")
    private String tenantCode;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 状态 0-试用 1-正常 2-冻结 3-注销
     */
    @TableField("status")
    private Byte status;

    /**
     * 套餐ID
     */
    @TableField("package_id")
    private Long packageId;

    /**
     * 试用到期时间
     */
    @TableField("trial_expire_time")
    private LocalDateTime trialExpireTime;

    /**
     * 付费到期时间
     */
    @TableField("paid_expire_time")
    private LocalDateTime paidExpireTime;

    /**
     * 冻结时间
     */
    @TableField("frozen_time")
    private LocalDateTime frozenTime;

    /**
     * 冻结原因
     */
    @TableField("frozen_reason")
    private String frozenReason;

    /**
     * 租户管理员用户ID
     */
    @TableField("admin_user_id")
    private Long adminUserId;

    /**
     * 企业Logo
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 企业地址
     */
    @TableField("address")
    private String address;
}
