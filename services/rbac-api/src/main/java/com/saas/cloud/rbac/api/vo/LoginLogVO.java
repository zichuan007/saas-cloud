package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class LoginLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户ID */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 登录类型 0-密码登录 1-短信登录 2-第三方登录 */
    private Integer loginType;

    /** 登录状态 0-失败 1-成功 */
    private Integer status;

    /** 登录IP */
    private String ip;

    /** 登录地点 */
    private String location;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 失败原因 */
    private String errorMsg;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
