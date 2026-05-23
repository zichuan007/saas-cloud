package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.entity.OperationLog;
import com.saas.cloud.rbac.service.IOperationLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 操作日志控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/operation-log")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OperationLogController {

    private final IOperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param module   操作模块（可选）
     * @param username 操作人（可选）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/list")
    public ApiResult<PageResult<OperationLog>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResult.ok(operationLogService.pageLogs(module, username, pageNum, pageSize));
    }

    /**
     * 清理操作日志（保留最近N天）
     *
     * @param keepDays 保留天数，默认90
     * @return 删除记录数
     */
    @Operation(summary = "清理操作日志")
    @com.saas.cloud.common.log.annotation.OperationLog(module = "操作日志", operation = "清理日志")
    @DeleteMapping("/clean")
    public ApiResult<Integer> clean(@RequestParam(defaultValue = "90") int keepDays) {
        return ApiResult.ok(operationLogService.cleanLogs(keepDays));
    }
}
