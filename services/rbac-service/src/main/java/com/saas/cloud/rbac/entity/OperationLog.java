package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_operation_log")
public class OperationLog extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @TableField("username")
    private String username;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作描述
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作类型（CREATE/UPDATE/DELETE 等）
     */
    @TableField("operate_type")
    private String operateType;

    /**
     * 请求方法
     */
    @TableField("method")
    private String method;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * HTTP方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 变更内容（字段 Diff）
     */
    @TableField("change_diff")
    private String changeDiff;

    /**
     * 响应状态码
     */
    @TableField("response_code")
    private Integer responseCode;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 操作IP
     */
    @TableField("ip")
    private String ip;

    /**
     * IP归属地
     */
    @TableField("location")
    private String location;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 执行时长(ms)
     */
    @TableField("duration")
    private Long duration;
}
