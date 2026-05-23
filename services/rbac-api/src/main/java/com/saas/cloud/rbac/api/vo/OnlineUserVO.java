package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线用户 视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class OnlineUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 租户ID */
    private Long tenantId;

    /** 部门ID */
    private Long deptId;

    /** 登录IP */
    private String ip;

    /** Token值（脱敏） */
    private String tokenValue;
}
