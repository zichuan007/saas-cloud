package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 套餐表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_package")
public class Package extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐名称
     */
    @TableField("package_name")
    private String packageName;

    /**
     * 套餐编码
     */
    @TableField("package_code")
    private String packageCode;

    /**
     * 月价格
     */
    @TableField("price_monthly")
    private BigDecimal priceMonthly;

    /**
     * 年价格
     */
    @TableField("price_yearly")
    private BigDecimal priceYearly;

    /**
     * 最大用户数 0-不限
     */
    @TableField("max_users")
    private Integer maxUsers;

    /**
     * 最大角色数 0-不限
     */
    @TableField("max_roles")
    private Integer maxRoles;

    /**
     * 最大部门数 0-不限
     */
    @TableField("max_depts")
    private Integer maxDepts;

    /**
     * 最大流程定义数 0-不限
     */
    @TableField("max_process_definitions")
    private Integer maxProcessDefinitions;

    /**
     * 最大公众号绑定数 0-不限
     */
    @TableField("max_wechat_accounts")
    private Integer maxWechatAccounts;

    /**
     * 最大存储空间(MB) 0-不限
     */
    @TableField("max_storage_mb")
    private Long maxStorageMb;

    /**
     * 可见菜单ID列表(JSON)
     */
    @TableField("menu_ids")
    private String menuIds;

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
