package com.saas.cloud.workflow.controller;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.workflow.api.dto.ProcessQueryDTO;
import com.saas.cloud.workflow.entity.WfProcessInstanceExt;
import com.saas.cloud.workflow.service.IWfProcessInstanceExtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 流程监控控制器（管理员）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "流程监控")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfMonitorController {

    private final IWfProcessInstanceExtService processInstanceExtService;

    /**
     * 分页查询运行中的流程实例
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询运行中的流程实例")
    @GetMapping("/instances")
    public ApiResult<PageResult<WfProcessInstanceExt>> instances(ProcessQueryDTO query) {
        return ApiResult.ok(processInstanceExtService.pageRunningInstances(query));
    }

    /**
     * 流程统计数据
     *
     * @return 统计信息
     */
    @Operation(summary = "流程统计数据")
    @GetMapping("/statistics")
    public ApiResult<Map<String, Object>> statistics() {
        Map<String, Object> result = new HashMap<>(4);
        result.put("running", processInstanceExtService.count(
                new LambdaQueryWrapper<WfProcessInstanceExt>().eq(WfProcessInstanceExt::getStatus, (byte) 0)));
        result.put("completed", processInstanceExtService.count(
                new LambdaQueryWrapper<WfProcessInstanceExt>().eq(WfProcessInstanceExt::getStatus, (byte) 1)));
        result.put("terminated", processInstanceExtService.count(
                new LambdaQueryWrapper<WfProcessInstanceExt>().in(WfProcessInstanceExt::getStatus, (byte) 2, (byte) 3)));
        LocalDateTime monthStart = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        result.put("monthlyStarted", processInstanceExtService.count(
                new LambdaQueryWrapper<WfProcessInstanceExt>().ge(WfProcessInstanceExt::getCreateTime, monthStart)));
        return ApiResult.ok(result);
    }

    /**
     * 强制终止流程
     *
     * @param id     扩展表主键ID
     * @param reason 终止原因
     * @return 操作结果
     */
    @Operation(summary = "强制终止流程")
    @OperationLog(module = "流程监控", operation = "强制终止流程")
    @PostMapping("/{id}/terminate")
    public ApiResult<Void> terminate(@PathVariable("id") Long id,
                                     @RequestParam(value = "reason", required = false) String reason) {
        processInstanceExtService.terminateProcess(id, reason);
        return ApiResult.ok();
    }
}
