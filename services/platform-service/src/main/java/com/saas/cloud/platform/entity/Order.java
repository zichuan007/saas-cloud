package com.saas.cloud.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 订单表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("sys_order")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    @TableField("tenant_id")
    private Long tenantId;

    /** 套餐ID */
    @TableField("package_id")
    private Long packageId;

    /** 订单编号 */
    @TableField("order_no")
    private String orderNo;

    /** 订单类型 0-新购 1-续费 2-升级 */
    @TableField("order_type")
    private Integer orderType;

    /** 订单金额 */
    @TableField("amount")
    private BigDecimal amount;

    /** 支付状态 0-待支付 1-已支付 2-已取消 */
    @TableField("pay_status")
    private Integer payStatus;

    /** 支付渠道 alipay/wechat/manual */
    @TableField("pay_channel")
    private String payChannel;

    /** 支付时间 */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /** 服务到期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
