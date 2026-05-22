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
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Component
public class SentinelRuleInitializer {

    @PostConstruct
    public void initRules() {

        List<FlowRule> rules = new ArrayList<>();
        rules.add(createFlowRule("/process/start", 50));
        rules.add(createFlowRule("/process/list", 100));
        rules.add(createFlowRule("/task/list", 100));
        rules.add(createFlowRule("/task/approve", 50));
        rules.add(createFlowRule("/definition/list", 100));
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
