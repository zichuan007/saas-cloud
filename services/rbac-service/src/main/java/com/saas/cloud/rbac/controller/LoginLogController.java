package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.LoginLogQueryDTO;
import com.saas.cloud.rbac.service.ILoginLogService;
import com.saas.cloud.rbac.api.vo.LoginLogVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录日志 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/login-log")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LoginLogController {

    private final ILoginLogService loginLogService;

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询条件（支持按用户名、状态、时间范围筛选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询登录日志")
    @GetMapping("/page")
    public ApiResult<PageResult<LoginLogVO>> page(LoginLogQueryDTO queryDTO) {
        return ApiResult.ok(loginLogService.queryPage(queryDTO));
    }

    /**
     * 清理登录日志（保留最近N天）
     *
     * @param keepDays 保留天数，默认90天
     * @return 清理条数
     */
    @Operation(summary = "清理登录日志")
    @OperationLog(module = "登录日志", operation = "清理日志")
    @DeleteMapping("/clean")
    public ApiResult<Integer> clean(@RequestParam(defaultValue = "90") int keepDays) {
        return ApiResult.ok(loginLogService.cleanLogs(keepDays));
    }
}
