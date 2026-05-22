package com.saas.cloud.platform.api.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 平台用户视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class PlatformUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色类型 0-超管 1-运营 */
    private Byte roleType;

    /** 角色类型描述 */
    private String roleTypeDesc;
}
