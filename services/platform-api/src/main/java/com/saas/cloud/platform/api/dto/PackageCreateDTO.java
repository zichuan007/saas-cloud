package com.saas.cloud.platform.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐创建/更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class PackageCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套餐名称 */
    @NotBlank(message = "套餐名称不能为空")
    private String packageName;

    /** 套餐编码 */
    @NotBlank(message = "套餐编码不能为空")
    private String packageCode;

    /** 月价格 */
    private BigDecimal priceMonthly;

    /** 年价格 */
    private BigDecimal priceYearly;

    /** 最大用户数，0表示不限 */
    @NotNull(message = "最大用户数不能为空")
    private Integer maxUsers;

    /** 最大角色数，0表示不限 */
    @NotNull(message = "最大角色数不能为空")
    private Integer maxRoles;

    /** 最大部门数，0表示不限 */
    @NotNull(message = "最大部门数不能为空")
    private Integer maxDepts;

    /** 最大流程定义数，0表示不限 */
    private Integer maxProcessDefinitions;

    /** 最大公众号绑定数，0表示不限 */
    private Integer maxWechatAccounts;

    /** 最大存储空间(MB)，0表示不限 */
    private Long maxStorageMb;

    /** 可见菜单ID列表(JSON) */
    private String menuIds;

    /** 排序 */
    private Integer sortOrder;
}
