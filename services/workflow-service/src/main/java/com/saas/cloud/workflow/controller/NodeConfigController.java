package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.api.ApiResult;
import com.saas.cloud.workflow.api.dto.NodeCandidateDTO;
import com.saas.cloud.workflow.api.dto.NodeConfigDTO;
import com.saas.cloud.workflow.tunnel.mapper.FlowNodeConfigMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowNodeCandidateMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeConfigDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeCandidateDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节点配置 Controller
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-08-14
 */
@Tag(name = "节点配置")
@RestController
@RequestMapping("/node-config")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NodeConfigController {

    private final FlowNodeConfigDAO nodeConfigDAO;
    private final FlowNodeCandidateMapper candidateMapper;

    @Operation(summary = "查询节点配置列表")
    @GetMapping("/{processDefKey}")
    public ApiResult<List<FlowNodeConfigDO>> list(@PathVariable String processDefKey) {
        FlowNodeConfigDO condition = new FlowNodeConfigDO();
        condition.setProcessDefKey(processDefKey);
        return ApiResult.success(nodeConfigMapper.selectList(condition));
    }

    @Operation(summary = "查询节点候选人列表")
    @GetMapping("/{processDefKey}/{nodeDefKey}/candidates")
    public ApiResult<List<FlowNodeCandidateDO>> candidates(
            @PathVariable String processDefKey, @PathVariable String nodeDefKey) {
        return ApiResult.success(candidateMapper.selectByProcessDefKeyAndNodeDefKey(processDefKey, nodeDefKey));
    }

    @Operation(summary = "批量保存节点配置(含候选人)")
    @PostMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> batchSave(@Valid @RequestBody List<NodeConfigDTO> configs) {
        for (NodeConfigDTO dto : configs) {
            // 创建节点配置
            FlowNodeConfigDO config = new FlowNodeConfigDO();
            config.setProcessDefKey(dto.getProcessDefKey());
            config.setNodeDefKey(dto.getNodeDefKey());
            config.setNodeName(dto.getNodeName());
            config.setApproveMode(dto.getApproveMode());
            config.setRejectMode(dto.getRejectMode());
            config.setTimeoutEnabled(dto.getTimeoutEnabled());
            config.setTimeoutHours(dto.getTimeoutHours());
            config.setTimeoutStrategy(dto.getTimeoutStrategy());
            config.setSameApproverSkip(dto.getSameApproverSkip());
            config.setEnabled(dto.getEnabled());
            config.setSortOrder(dto.getSortOrder());
            nodeConfigMapper.insert(config);

            // 创建候选人来源
            if (dto.getCandidates() != null) {
                for (NodeCandidateDTO can : dto.getCandidates()) {
                    FlowNodeCandidateDO candidate = new FlowNodeCandidateDO();
                    candidate.setNodeConfigId(config.getId());
                    candidate.setProcessDefKey(dto.getProcessDefKey());
                    candidate.setNodeDefKey(dto.getNodeDefKey());
                    candidate.setAssignType(can.getAssignType());
                    candidate.setAssignValue(can.getAssignValue());
                    candidate.setSortOrder(can.getSortOrder());
                    candidateMapper.insert(candidate);
                }
            }
        }
        return ApiResult.success();
    }

    @Operation(summary = "删除节点配置")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable Long id) {
        nodeConfigMapper.deleteById(id);
        return ApiResult.success();
    }
}