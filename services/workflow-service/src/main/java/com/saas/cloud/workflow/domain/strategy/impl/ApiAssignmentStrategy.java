package com.saas.cloud.workflow.domain.strategy.impl;

import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.api.enums.AssignTypeEnum;
import com.saas.cloud.workflow.domain.strategy.AssignmentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Api 分配策略
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
public class ApiAssignmentStrategy implements AssignmentStrategy {

    @Override
    public AssignTypeEnum getType() {
        return AssignTypeEnum.API;
    }

    @Override
    public List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables) {
        // 调用外部 HTTP 接口获取审批人
        // 内置超时配置: 连接超时3s, 读取超时5s
        log.warn("API策略未完整实现，URL: {}", assignValue);
        return Collections.emptyList();
    }
}