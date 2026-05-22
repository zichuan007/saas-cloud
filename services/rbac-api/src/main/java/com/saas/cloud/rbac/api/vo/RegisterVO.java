package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 租户注册响应VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class RegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 租户编码 */
    private String tenantCode;

    /** 用户ID */
    private Long userId;

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 过期时间（秒） */
    private Long expiresIn;
}
