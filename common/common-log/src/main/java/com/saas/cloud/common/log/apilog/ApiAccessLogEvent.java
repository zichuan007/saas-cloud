package com.saas.cloud.common.log.apilog;

import java.io.Serializable;

import lombok.Data;

/**
 * API 访问日志事件 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Data
public class ApiAccessLogEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 链路追踪 ID */
    private String traceId;

    /** 操作用户 ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 租户 ID */
    private Long tenantId;

    /** 请求 URL */
    private String requestUrl;

    /** HTTP 方法 */
    private String requestMethod;

    /** 查询参数 */
    private String queryString;

    /** 请求 IP */
    private String ip;

    /** 用户代理 */
    private String userAgent;

    /** HTTP 响应状态码 */
    private Integer httpStatus;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 请求时间戳 */
    private Long requestTime;
}
