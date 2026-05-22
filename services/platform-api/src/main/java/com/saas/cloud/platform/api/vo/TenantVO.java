package com.saas.cloud.platform.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 租户信息视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TenantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long id;

    /** 租户编码 */
    private String tenantCode;

    /** 租户名称 */
    private String tenantName;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 联系邮箱 */
    private String contactEmail;

    /** 租户状态 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 套餐ID */
    private Long packageId;

    /** 套餐名称 */
    private String packageName;

    /** 套餐可见菜单ID列表(JSON数组) */
    private String menuIds;

    /** 最大用户数 */
    private Integer maxUsers;

    /** 当前用户数 */
    private Integer currentUsers;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
