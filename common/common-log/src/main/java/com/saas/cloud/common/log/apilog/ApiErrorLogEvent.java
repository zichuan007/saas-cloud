package com.saas.cloud.common.log.apilog;

import java.io.Serializable;

import lombok.Data;

/**
 * API 错误日志事件 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Data
public class ApiErrorLogEvent implements Serializable {

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

    /** 异常类名 */
    private String exceptionName;

    /** 异常信息 */
    private String exceptionMessage;

    /** 异常堆栈（截断） */
    private String exceptionStackTrace;

    /** 发生时间戳 */
    private Long timestamp;
}
