package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.api.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.api.dto.request.CancelRequest;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessInstanceExtMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessDefExtMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessInstanceExtDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessDefExtDO;
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
    private final FlowProcessInstanceExtDAO processInstanceExtDAO;
    private final FlowProcessDefExtDAO processDefExtDAO;

    @Operation(summary = "可发起流程列表")
    @GetMapping("/startable-list")
    public ApiResult<List<FlowProcessDefExtDO>> startableList() {
        FlowProcessDefExtDO condition = new FlowProcessDefExtDO();
        condition.setStatus(1);
        return ApiResult.success(processDefExtMapper.selectList(condition));
    }

    @Operation(summary = "我发起的流程")
    @GetMapping("/my-initiated")
    public ApiResult<List<FlowProcessInstanceExtDO>> myInitiated() {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.success(List.of())
                : ApiResult.success(processInstanceExtMapper.selectByInitiatorId(userId));
    }

    @Operation(summary = "流程详情")
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.error("流程实例不存在");
        List<Map<String, Object>> activities = processEngine.queryHistoricActivityInstances(instance.getProcessInstanceId());
        return ApiResult.success(Map.of("instance", instance, "timeline", activities));
    }

    @Operation(summary = "流程图高亮")
    @GetMapping("/{id}/diagram")
    public ApiResult<Map<String, Object>> diagram(@PathVariable Long id) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.error("流程实例不存在");
        return ApiResult.success(Map.of(
                "processDefinitionId", instance.getProcessDefinitionId(),
                "activities", processEngine.queryHistoricActivityInstances(instance.getProcessInstanceId())
        ));
    }

    @Operation(summary = "撤回流程")
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.error("流程实例不存在");
        processEngine.deleteProcessInstance(instance.getProcessInstanceId(), request.getReason());
        return ApiResult.success();
    }

    @Operation(summary = "运行中实例")
    @GetMapping("/monitor/instances")
    public ApiResult<List<FlowProcessInstanceExtDO>> runningInstances() {
        return ApiResult.success(processInstanceExtMapper.selectByStatus(0));
    }

    @Operation(summary = "流程统计")
    @GetMapping("/monitor/statistics")
    public ApiResult<Map<String, Object>> statistics() {
        return ApiResult.success(Map.of(
                "running", processInstanceExtMapper.countByStatus(0),
                "completed", processInstanceExtMapper.countByStatus(1),
                "terminated", processInstanceExtMapper.countByStatus(3)
        ));
    }

    @Operation(summary = "强制终止流程")
    @PostMapping("/monitor/{id}/terminate")
    public ApiResult<Void> terminate(@PathVariable Long id, @RequestParam String reason) {
        FlowProcessInstanceExtDO instance = processInstanceExtMapper.selectById(id);
        if (instance == null) return ApiResult.error("流程实例不存在");
        processEngine.deleteProcessInstance(instance.getProcessInstanceId(), reason);
        return ApiResult.success();
    }
}