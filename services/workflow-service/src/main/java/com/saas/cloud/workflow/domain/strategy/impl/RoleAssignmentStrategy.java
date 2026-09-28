package com.saas.cloud.workflow.domain.strategy.impl;

import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.api.enums.AssignTypeEnum;
import com.saas.cloud.workflow.domain.strategy.AssignmentStrategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Role 分配策略
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class RoleAssignmentStrategy implements AssignmentStrategy {

    @Override
    public AssignTypeEnum getType() {
        return AssignTypeEnum.ROLE;
    }

    @Override
    public List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables) {
        // 委托 RBAC 服务查询角色成员
        // 实际查询由 TaskAssignmentDomainService 完成
        return Collections.singletonList(
                AssigneeDTO.builder().userId(assignValue).build()
        );
    }
}