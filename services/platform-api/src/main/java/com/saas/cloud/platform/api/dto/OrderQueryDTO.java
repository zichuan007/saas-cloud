package com.saas.cloud.platform.api.dto;

import lombok.Data;

/**
 * 订单查询 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Data
public class OrderQueryDTO {

    /** 租户ID（平台端按租户筛选） */
    private Long tenantId;

    /** 支付状态 */
    private Integer payStatus;

    /** 页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;
}
