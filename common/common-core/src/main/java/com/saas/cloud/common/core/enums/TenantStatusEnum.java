package com.saas.cloud.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租户状态枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Getter
@AllArgsConstructor
public enum TenantStatusEnum {

    TRIAL(0, "试用"),
    ACTIVE(1, "正常"),
    FROZEN(2, "冻结"),
    DEACTIVATED(3, "注销");

    private final int code;
    private final String desc;

    public static TenantStatusEnum of(int code) {
        for (TenantStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知租户状态: " + code);
    }
}
