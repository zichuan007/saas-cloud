package com.saas.cloud.common.core.exception;

import com.saas.cloud.common.core.result.ResultCode;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
