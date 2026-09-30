package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionCreateDTO;
import com.saas.cloud.workflow.api.dto.ProcessDeployDTO;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessDefExtMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessDefExtDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "流程定义")
@RestController
@RequestMapping("/definition")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DefinitionController {

    private final ProcessEngineGateway processEngine;
    private final FlowProcessDefExtMapper processDefExtMapper;
    private final RepositoryService repositoryService;

    @Operation(summary = "流程定义列表")
    @GetMapping("/list")
    public ApiResult<List<FlowProcessDefExtDO>> list() {
        return ApiResult.ok(processDefExtMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @Operation(summary = "流程定义详情")
    @GetMapping("/{id}")
    public ApiResult<FlowProcessDefExtDO> detail(@PathVariable Long id) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        return def == null ? ApiResult.fail("流程定义不存在") : ApiResult.ok(def);
    }

    @Operation(summary = "创建流程定义")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@Valid @RequestBody ProcessDefinitionCreateDTO dto) {
        FlowProcessDefExtDO def = new FlowProcessDefExtDO();
        def.setProcessKey(dto.getProcessKey());
        def.setProcessName(dto.getProcessName());
        def.setCategory(dto.getCategory());
        def.setDescription(dto.getDescription());
        def.setFormUrl(dto.getFormUrl());
        def.setSortOrder(dto.getSortOrder());
        def.setStatus(1);
        processDefExtMapper.insert(def);
        return ApiResult.ok();
    }

    @Operation(summary = "更新流程定义")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody ProcessDefinitionCreateDTO dto) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        if (def == null) return ApiResult.fail("流程定义不存在");
        def.setProcessKey(dto.getProcessKey());
        def.setProcessName(dto.getProcessName());
        def.setCategory(dto.getCategory());
        def.setDescription(dto.getDescription());
        def.setFormUrl(dto.getFormUrl());
        def.setSortOrder(dto.getSortOrder());
        processDefExtMapper.updateById(def);
        return ApiResult.ok();
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable Long id) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        if (def != null && def.getProcessDefinitionId() != null) {
            repositoryService.deleteDeployment(def.getProcessDefinitionId(), true);
        }
        processDefExtMapper.deleteById(id);
        return ApiResult.ok();
    }

    @Operation(summary = "部署流程定义")
    @PostMapping("/{id}/deploy")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> deploy(@PathVariable Long id, @Valid @RequestBody ProcessDeployDTO dto) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        if (def == null) return ApiResult.fail("流程定义不存在");
        var deployment = repositoryService.createDeployment()
                .addString(def.getProcessKey() + ".bpmn20.xml", dto.getBpmnXml())
                .name(def.getProcessName())
                .deploy();
        def.setProcessDefinitionId(deployment.getId());
        processDefExtMapper.updateById(def);
        return ApiResult.ok();
    }

    @Operation(summary = "挂起/激活流程定义")
    @PutMapping("/{id}/status")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        if (def == null || def.getProcessDefinitionId() == null) return ApiResult.fail("流程定义不存在或未部署");
        if (status == 0) processEngine.suspendProcessDefinition(def.getProcessDefinitionId());
        else processEngine.activateProcessDefinition(def.getProcessDefinitionId());
        return ApiResult.ok();
    }

    @Operation(summary = "获取BPMN XML")
    @GetMapping("/{id}/bpmn-xml")
    public ApiResult<String> getBpmnXml(@PathVariable Long id) {
        FlowProcessDefExtDO def = processDefExtMapper.selectById(id);
        if (def == null || def.getProcessDefinitionId() == null) return ApiResult.fail("流程定义未部署");
        return ApiResult.ok(processEngine.getProcessModel(def.getProcessDefinitionId()));
    }
}