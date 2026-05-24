package com.saas.cloud.common.log.event;

import java.io.Serializable;

import lombok.Data;

/**
 * 操作日志事件 DTO（Kafka 消息体）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class OperationLogEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 租户ID */
    private Long tenantId;

    /** 操作模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 操作类型 */
    private String operateType;

    /** 类名.方法名 */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** HTTP 方法（GET/POST/PUT/DELETE） */
    private String requestMethod;

    /** JSON 化的请求参数 */
    private String requestParams;

    /** 响应状态码（HTTP 状态或业务码） */
    private Integer responseCode;

    /** 错误信息 */
    private String errorMsg;

    /** 操作 IP 地址 */
    private String ip;

    /** IP 归属地 */
    private String location;

    /** 用户代理 */
    private String userAgent;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 事件发生时间戳 */
    private Long timestamp;
}
