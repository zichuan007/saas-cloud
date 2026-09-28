package com.saas.cloud.workflow.domain.strategy;

import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.api.enums.AssignTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * 审批人分配策略接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public interface AssignmentStrategy {

    /**
     * 策略类型
     */
    AssignTypeEnum getType();

    /**
     * 解析审批人
     *
     * @param assignValue 策略配置值 (用户ID/角色编码/审批组前缀/表达式/API URL)
     * @param variables   流程变量
     * @return 审批人列表
     */
    List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables);
}