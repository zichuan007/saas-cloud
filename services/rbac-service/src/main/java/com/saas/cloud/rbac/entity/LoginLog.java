package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 登录日志表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_login_log")
public class LoginLog extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID（登录成功时） */
    @TableField("user_id")
    private Long userId;

    /** 登录用户名 */
    @TableField("username")
    private String username;

    /** 登录类型 0-密码登录 1-短信登录 2-第三方登录 */
    @TableField("login_type")
    private Integer loginType;

    /** 登录状态 0-失败 1-成功 */
    @TableField("status")
    private Integer status;

    /** 登录IP */
    @TableField("ip")
    private String ip;

    /** 登录地点 */
    @TableField("location")
    private String location;

    /** 浏览器 */
    @TableField("browser")
    private String browser;

    /** 操作系统 */
    @TableField("os")
    private String os;

    /** User-Agent */
    @TableField("user_agent")
    private String userAgent;

    /** 失败原因 */
    @TableField("error_msg")
    private String errorMsg;

    /** 登录时间 */
    @TableField("login_time")
    private LocalDateTime loginTime;
}
