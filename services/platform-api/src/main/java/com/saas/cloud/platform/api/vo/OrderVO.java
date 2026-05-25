package com.saas.cloud.platform.api.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 订单 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Data
public class OrderVO {

    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 租户名称（关联查询） */
    private String tenantName;

    /** 套餐ID */
    private Long packageId;

    /** 套餐名称（关联查询） */
    private String packageName;

    /** 订单编号 */
    private String orderNo;

    /** 订单类型 0-新购 1-续费 2-升级 */
    private Integer orderType;

    /** 订单金额 */
    private BigDecimal amount;

    /** 支付状态 0-待支付 1-已支付 2-已取消 */
    private Integer payStatus;

    /** 支付渠道 */
    private String payChannel;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 服务到期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
