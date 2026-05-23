package com.saas.cloud.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.workflow.api.dto.TaskAddSignDTO;
import com.saas.cloud.workflow.api.dto.TaskApproveDTO;
import com.saas.cloud.workflow.api.dto.TaskDelegateDTO;
import com.saas.cloud.workflow.api.dto.TaskQueryDTO;
import com.saas.cloud.workflow.api.dto.TaskRejectDTO;
import com.saas.cloud.workflow.api.dto.TaskTransferDTO;
import com.saas.cloud.workflow.entity.WfCopy;
import com.saas.cloud.workflow.entity.WfTaskExt;
import com.saas.cloud.workflow.service.IWfCopyService;
import com.saas.cloud.workflow.service.IWfTaskExtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 任务审批控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfTaskExtController {

    private final IWfTaskExtService taskExtService;
    private final IWfCopyService copyService;

    /**
     * 分页查询我的待办任务
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询我的待办任务")
    @GetMapping("/todo")
    public ApiResult<PageResult<WfTaskExt>> todo(TaskQueryDTO query) {
        return ApiResult.ok(taskExtService.pageTodoTasks(query));
    }

    /**
     * 分页查询我的已办任务
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询我的已办任务")
    @GetMapping("/done")
    public ApiResult<PageResult<WfTaskExt>> done(TaskQueryDTO query) {
        return ApiResult.ok(taskExtService.pageDoneTasks(query));
    }

    /**
     * 分页查询抄送给我的记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询抄送给我的记录")
    @GetMapping("/copy")
    public ApiResult<PageResult<WfCopy>> copy(TaskQueryDTO query) {
        return ApiResult.ok(copyService.pageMyCopies(query));
    }

    /**
     * 审批通过
     *
     * @param id  Flowable任务ID
     * @param dto 审批请求
     * @return 操作结果
     */
    @Operation(summary = "审批通过")
    @OperationLog(module = "流程审批", operation = "审批通过")
    @PostMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable("id") String id,
                                   @Validated @RequestBody TaskApproveDTO dto) {
        dto.setTaskId(id);
        taskExtService.approve(dto);
        return ApiResult.ok();
    }

    /**
     * 驳回
     *
     * @param id  Flowable任务ID
     * @param dto 驳回请求
     * @return 操作结果
     */
    @Operation(summary = "驳回")
    @OperationLog(module = "流程审批", operation = "审批驳回")
    @PostMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable("id") String id,
                                  @Validated @RequestBody TaskRejectDTO dto) {
        dto.setTaskId(id);
        taskExtService.reject(dto);
        return ApiResult.ok();
    }

    /**
     * 转办
     *
     * @param id  Flowable任务ID
     * @param dto 转办请求
     * @return 操作结果
     */
    @Operation(summary = "转办")
    @OperationLog(module = "流程审批", operation = "转办任务")
    @PostMapping("/{id}/transfer")
    public ApiResult<Void> transfer(@PathVariable("id") String id,
                                    @Validated @RequestBody TaskTransferDTO dto) {
        dto.setTaskId(id);
        taskExtService.transfer(dto);
        return ApiResult.ok();
    }

    /**
     * 委派
     *
     * @param id  Flowable任务ID
     * @param dto 委派请求
     * @return 操作结果
     */
    @Operation(summary = "委派")
    @OperationLog(module = "流程审批", operation = "委派任务")
    @PostMapping("/{id}/delegate")
    public ApiResult<Void> delegate(@PathVariable("id") String id,
                                    @Validated @RequestBody TaskDelegateDTO dto) {
        dto.setTaskId(id);
        taskExtService.delegate(dto);
        return ApiResult.ok();
    }

    /**
     * 加签
     *
     * @param id  Flowable任务ID
     * @param dto 加签请求
     * @return 操作结果
     */
    @Operation(summary = "加签")
    @OperationLog(module = "流程审批", operation = "加签")
    @PostMapping("/{id}/add-sign")
    public ApiResult<Void> addSign(@PathVariable("id") String id,
                                   @Validated @RequestBody TaskAddSignDTO dto) {
        dto.setTaskId(id);
        taskExtService.addSign(dto);
        return ApiResult.ok();
    }

    /**
     * 催办
     *
     * @param id Flowable任务ID
     * @return 操作结果
     */
    @Operation(summary = "催办")
    @PostMapping("/{id}/urge")
    public ApiResult<Void> urge(@PathVariable("id") String id) {
        taskExtService.urge(id);
        return ApiResult.ok();
    }

    /**
     * 标记抄送记录已读
     *
     * @param id 抄送记录主键
     * @return 操作结果
     */
    @Operation(summary = "标记抄送记录已读")
    @PutMapping("/copy/{id}/read")
    public ApiResult<Void> markCopyAsRead(@PathVariable("id") Long id) {
        copyService.markAsRead(id);
        return ApiResult.ok();
    }
}
