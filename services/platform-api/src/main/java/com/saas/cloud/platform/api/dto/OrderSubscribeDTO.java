package com.saas.cloud.platform.api.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * 订购/续费请求 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Data
public class OrderSubscribeDTO {

    /** 套餐ID */
    @NotNull(message = "套餐ID不能为空")
    private Long packageId;

    /** 订单类型 0-新购 1-续费 2-升级 */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    /** 支付渠道 alipay/wechat/manual */
    private String payChannel;

    /** 购买时长（月数），默认 1 */
    private Integer months;
}
