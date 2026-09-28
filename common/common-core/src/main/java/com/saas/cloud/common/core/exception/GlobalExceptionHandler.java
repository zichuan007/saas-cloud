package com.saas.cloud.common.core.exception;

import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Void> handleForbiddenException(ForbiddenException e) {
        return ApiResult.fail(ResultCode.FORBIDDEN.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 请求体不可读/JSON 格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败: {}", request.getRequestURI());
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误或缺失");
    }

    /**
     * 缺少必填查询参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), "缺少必填参数: " + e.getParameterName());
    }

    /**
     * 方法参数校验失败（@Validated 路径/查询参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 方法级校验异常（@Validated 标在类上的整体校验）
     */
    @ExceptionHandler(MethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleMethodValidation(MethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 上传文件超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ApiResult.fail(ResultCode.BAD_REQUEST.getCode(), "上传文件超出大小限制");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleException(Exception e, HttpServletRequest request) {
        // 服务端记录完整堆栈，响应体不暴露内部细节
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResult.fail(ResultCode.INTERNAL_ERROR.getCode(), "服务器内部错误，请稍后再试");
    }
}
