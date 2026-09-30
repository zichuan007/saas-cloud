package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.api.dto.request.CancelRequest;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessInstanceExtMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessDefExtMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessInstanceExtDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessDefExtDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "流程实例")
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProcessInstanceController {

    private final ProcessEngineGateway processEngine;
    private final FlowProcessInstanceExtMapper processInstanceExtMapper;
    private final FlowProcessDefExtMapper processDefExtMapper;

    @Operation(summary = "可发起流程列表")
    @GetMapping("/startable-list")
    public ApiResult<List<FlowProcessDefExtDO>> startableList() {
        return ApiResult.ok(processDefExtMapper.selectList(
                new LambdaQueryWrapper<FlowProcessDefExtDO>()
                        .eq(FlowProcessDefExtDO::getStatus, 1)));
    }

    @Operation(summary = "我发起的流程")
    @GetMapping("/my-initiated")
    public ApiResult<List<FlowProcessInstanceExtDO>> myInitiated() {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.ok(List.of())
                : ApiResult.ok(processInstanceExtMapper.selectByInitiatorId(userId));
    }

    @Operation(summary = "流程详情")
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.fail("流程实例不存在");
        List<Map<String, Object>> activities = processEngine.queryHistoricActivityInstances(instance.getProcessInstanceId());
        return ApiResult.ok(Map.of("instance", instance, "timeline", activities));
    }

    @Operation(summary = "流程图高亮")
    @GetMapping("/{id}/diagram")
    public ApiResult<Map<String, Object>> diagram(@PathVariable Long id) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.fail("流程实例不存在");
        return ApiResult.ok(Map.of(
                "processDefinitionId", instance.getProcessDefinitionId(),
                "activities", processEngine.queryHistoricActivityInstances(instance.getProcessInstanceId())
        ));
    }

    @Operation(summary = "撤回流程")
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.fail("流程实例不存在");
        processEngine.deleteProcessInstance(instance.getProcessInstanceId(), request.getReason());
        return ApiResult.ok();
    }

    @Operation(summary = "运行中实例")
    @GetMapping("/monitor/instances")
    public ApiResult<List<FlowProcessInstanceExtDO>> runningInstances() {
        return ApiResult.ok(processInstanceExtMapper.selectByStatus(0));
    }

    @Operation(summary = "流程统计")
    @GetMapping("/monitor/statistics")
    public ApiResult<Map<String, Object>> statistics() {
        return ApiResult.ok(Map.of(
                "running", processInstanceExtMapper.countByStatus(0),
                "completed", processInstanceExtMapper.countByStatus(1),
                "terminated", processInstanceExtMapper.countByStatus(3)
        ));
    }

    @Operation(summary = "强制终止流程")
    @PostMapping("/monitor/{id}/terminate")
    public ApiResult<Void> terminate(@PathVariable Long id, @RequestParam String reason) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.fail("流程实例不存在");
        processEngine.deleteProcessInstance(instance.getProcessInstanceId(), reason);
        return ApiResult.ok();
    }
}