package com.saas.cloud.common.core.exception;

import com.saas.cloud.common.core.result.ResultCode;

/**
 * 权限不足异常
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ResultCode.FORBIDDEN, message);
    }
}
