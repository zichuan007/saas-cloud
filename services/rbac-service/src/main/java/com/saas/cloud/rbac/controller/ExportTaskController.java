package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.vo.ExportTaskVO;
import com.saas.cloud.rbac.service.IExportTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 下载中心（导出任务管理）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "下载中心")
@RestController
@RequestMapping("/export-task")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExportTaskController {

    private final IExportTaskService exportTaskService;

    @Operation(summary = "查询我的导出任务列表")
    @GetMapping("/list")
    public ApiResult<PageResult<ExportTaskVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResult.ok(exportTaskService.queryMyTasks(pageNum, pageSize));
    }

    @Operation(summary = "获取下载链接")
    @GetMapping("/{id}/download")
    public ApiResult<String> download(@PathVariable Long id) {
        return ApiResult.ok(exportTaskService.getDownloadUrl(id));
    }

    @Operation(summary = "删除导出任务")
    @OperationLog(module = "下载中心", operation = "删除导出任务")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        exportTaskService.deleteTask(id);
        return ApiResult.ok();
    }
}
