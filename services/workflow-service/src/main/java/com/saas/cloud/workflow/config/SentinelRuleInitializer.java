package com.saas.cloud.workflow.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 流控规则初始化
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-08-14
 */
@Slf4j
@Component
public class SentinelRuleInitializer {

    @PostConstruct
    public void initRules() {
        List<FlowRule> rules = new ArrayList<>();
        // 流程实例
        rules.add(createFlowRule("/process/startable-list", 100));
        rules.add(createFlowRule("/process/my-initiated", 100));
        // 任务操作
        rules.add(createFlowRule("/task/todo", 100));
        rules.add(createFlowRule("/task/done", 100));
        rules.add(createFlowRule("/task/{id}/approve", 50));
        rules.add(createFlowRule("/task/{id}/reject", 50));
        // 流程定义
        rules.add(createFlowRule("/definition/list", 100));
        rules.add(createFlowRule("/definition/{id}/deploy", 20));
        // 统计
        rules.add(createFlowRule("/statistics/overview", 50));

        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel] workflow-service 流控规则初始化完成, flowRules={}", rules.size());
    }

    private FlowRule createFlowRule(String resource, int qps) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        return rule;
    }
}