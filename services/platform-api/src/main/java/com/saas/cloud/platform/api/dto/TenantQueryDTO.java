package com.saas.cloud.platform.api.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 租户分页查询请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TenantQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户名称（模糊查询） */
    private String tenantName;

    /** 状态 */
    private Integer status;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
