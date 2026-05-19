package com.saas.cloud.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器错误"),

    TENANT_FROZEN(4031, "租户已冻结"),
    TENANT_EXPIRED(4032, "租户已过期"),
    QUOTA_EXCEEDED(4091, "配额已超限"),
    LOGIN_FAILED(4011, "用户名或密码错误"),
    ACCOUNT_LOCKED(4012, "账号已锁定"),
    ACCOUNT_DISABLED(4013, "账号已禁用"),
    TOKEN_EXPIRED(4014, "Token 已过期"),
    TOKEN_INVALID(4015, "Token 无效");

    private final int code;
    private final String message;
}
