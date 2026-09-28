package com.saas.cloud.workflow.domain.strategy.impl;

import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.api.enums.AssignTypeEnum;
import com.saas.cloud.workflow.domain.strategy.AssignmentStrategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 指定用户策略
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class UserAssignmentStrategy implements AssignmentStrategy {

    @Override
    public AssignTypeEnum getType() {
        return AssignTypeEnum.USER;
    }

    @Override
    public List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables) {
        if (assignValue == null || assignValue.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
                AssigneeDTO.builder().userId(assignValue).build()
        );
    }
}