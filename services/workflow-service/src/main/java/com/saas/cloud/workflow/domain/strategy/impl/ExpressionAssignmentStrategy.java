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
 * Expression 分配策略
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
public class ExpressionAssignmentStrategy implements AssignmentStrategy {

    @Override
    public AssignTypeEnum getType() {
        return AssignTypeEnum.EXPRESSION;
    }

    @Override
    public List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables) {
        // 使用 SimpleEvaluationContext 安全解析 SpEL 表达式
        // 禁止 T()、java.、Runtime、Process 等危险调用
        try {
            org.springframework.expression.spel.standard.SpelExpressionParser parser =
                    new org.springframework.expression.spel.standard.SpelExpressionParser();
            org.springframework.expression.EvaluationContext context =
                    org.springframework.expression.spel.support.SimpleEvaluationContext.Builder()
                            .withRootObject(variables)
                            .build();
            String userId = parser.parseExpression(assignValue).getValue(context, String.class);
            if (userId == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(
                    AssigneeDTO.builder().userId(userId).build()
            );
        } catch (Exception e) {
            log.error("SpEL表达式解析失败: {}", assignValue, e);
            return Collections.emptyList();
        }
    }
}