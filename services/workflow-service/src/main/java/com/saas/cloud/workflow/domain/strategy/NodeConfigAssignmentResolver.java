package com.saas.cloud.workflow.domain.strategy;

import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeCandidateDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批人分配策略调度器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NodeConfigAssignmentResolver {

    private final List<AssignmentStrategy> strategies;

    /**
     * 根据节点配置解析审批人列表
     *
     * @param nodeConfig 节点配置
     * @param candidates 候选人来源列表
     * @param variables  流程变量
     * @return 去重后的审批人列表
     */
    public List<AssigneeDTO> resolve(FlowNodeConfigDO nodeConfig,
                                      List<FlowNodeCandidateDO> candidates,
                                      Map<String, Object> variables) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 按 sortOrder 排序
        candidates.sort(Comparator.comparingInt(FlowNodeCandidateDO::getSortOrder));

        Map<String, AssigneeDTO> assigneeMap = new LinkedHashMap<>();

        for (FlowNodeCandidateDO candidate : candidates) {
            for (AssignmentStrategy strategy : strategies) {
                if (strategy.getType().getCode().equals(candidate.getAssignType())) {
                    try {
                        List<AssigneeDTO> resolved = strategy.resolve(candidate.getAssignValue(), variables);
                        for (AssigneeDTO dto : resolved) {
                            // 按 userId 去重
                            assigneeMap.putIfAbsent(dto.getUserId(), dto);
                        }
                    } catch (Exception e) {
                        log.error("解析审批人失败: nodeConfig={}, candidate={}", nodeConfig.getNodeDefKey(), candidate.getAssignType(), e);
                    }
                    break;
                }
            }
        }

        return new ArrayList<>(assigneeMap.values());
    }
}