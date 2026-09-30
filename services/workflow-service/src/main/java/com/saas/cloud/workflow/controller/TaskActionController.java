package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.api.dto.request.*;
import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.domain.service.action.ApprovalAction;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalRecordMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowCcRecordMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowCcRecordDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务管理 Controller
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-08-14
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
public class TaskActionController {

    private final Map<ApprovalActionEnum, ApprovalAction<?>> actionMap;
    private final FlowApprovalRecordMapper approvalRecordMapper;
    private final FlowCcRecordMapper ccRecordMapper;
    private final ProcessEngineGateway processEngine;

    @Autowired
    public TaskActionController(List<ApprovalAction<?>> actions,
                                 FlowApprovalRecordMapper approvalRecordMapper,
                                 FlowCcRecordMapper ccRecordMapper,
                                 ProcessEngineGateway processEngine) {
        this.actionMap = actions.stream()
                .collect(Collectors.toMap(ApprovalAction::getActionType, Function.identity()));
        this.approvalRecordMapper = approvalRecordMapper;
        this.ccRecordMapper = ccRecordMapper;
        this.processEngine = processEngine;
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{taskId}")
    public ApiResult<Map<String, Object>> detail(@PathVariable String taskId) {
        Map<String, Object> task = processEngine.queryTask(taskId);
        return task == null ? ApiResult.fail("任务不存在") : ApiResult.ok(task);
    }

    @Operation(summary = "任务评论")
    @GetMapping("/{taskId}/comments")
    public ApiResult<List<Map<String, Object>>> comments(@PathVariable String taskId) {
        return ApiResult.ok(processEngine.getComments(taskId));
    }

    @Operation(summary = "我的待办任务")
    @GetMapping("/todo")
    public ApiResult<List<Map<String, Object>>> todo() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResult.fail("未登录");
        FlowApprovalRecordDO condition = new FlowApprovalRecordDO();
        condition.setOperatorId(userId);
        return ApiResult.ok(List.of());
    }

    @Operation(summary = "我的已办任务")
    @GetMapping("/done")
    public ApiResult<List<FlowApprovalRecordDO>> done() {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.ok(List.of())
                : ApiResult.ok(approvalRecordMapper.selectByOperatorId(userId));
    }

    @Operation(summary = "抄送给我的")
    @GetMapping("/copy")
    public ApiResult<List<FlowCcRecordDO>> copy() {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.ok(List.of())
                : ApiResult.ok(ccRecordMapper.selectByReceiverId(userId));
    }

    @Operation(summary = "标记抄送已读")
    @PutMapping("/copy/{id}/read")
    public ApiResult<Void> markCopyAsRead(@PathVariable Long id) {
        FlowCcRecordDO record = ccRecordMapper.selectById(id);
        if (record != null) {
            record.setIsRead(1);
            ccRecordMapper.updateById(record);
        }
        return ApiResult.ok();
    }

    @Operation(summary = "审批通过")
    @PostMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable String id, @Valid @RequestBody ApproveRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.APPROVED).execute(request);
        return ApiResult.ok();
    }

    @Operation(summary = "驳回")
    @PostMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable String id, @Valid @RequestBody RejectRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.REJECTED).execute(request);
        return ApiResult.ok();
    }

    @Operation(summary = "转办")
    @PostMapping("/{id}/transfer")
    public ApiResult<Void> transfer(@PathVariable String id, @Valid @RequestBody TransferRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.TRANSFER).execute(request);
        return ApiResult.ok();
    }

    @Operation(summary = "委派")
    @PostMapping("/{id}/delegate")
    public ApiResult<Void> delegate(@PathVariable String id, @Valid @RequestBody DelegateRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.DELEGATE).execute(request);
        return ApiResult.ok();
    }

    @Operation(summary = "加签")
    @PostMapping("/{id}/add-sign")
    public ApiResult<Void> addSign(@PathVariable String id, @Valid @RequestBody AddSignRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.ADD_SIGN).execute(request);
        return ApiResult.ok();
    }

    @Operation(summary = "催办")
    @PostMapping("/{id}/urge")
    public ApiResult<Void> urge(@PathVariable String id, @Valid @RequestBody UrgeRequest request) {
        request.setTaskId(id);
        getAction(ApprovalActionEnum.URGE).execute(request);
        return ApiResult.ok();
    }

    @SuppressWarnings("unchecked")
    private <T> ApprovalAction<T> getAction(ApprovalActionEnum type) {
        ApprovalAction<?> action = actionMap.get(type);
        if (action == null) throw new IllegalArgumentException("不支持的动作类型: " + type);
        return (ApprovalAction<T>) action;
    }
}