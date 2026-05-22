package com.saas.cloud.rbac.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
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

        initFlowRules();
        initDegradeRules();
        log.info("[Sentinel] rbac-service 流控规则初始化完成, flowRules={}, degradeRules={}",
                FlowRuleManager.getRules().size(), DegradeRuleManager.getRules().size());
    }

    private void initFlowRules() {

        List<FlowRule> rules = new ArrayList<>();
        rules.add(createFlowRule("/auth/login", 50));
        rules.add(createFlowRule("/auth/register", 10));
        rules.add(createFlowRule("/auth/refresh", 30));
        rules.add(createFlowRule("/user/list", 100));
        rules.add(createFlowRule("/user/password", 20));
        rules.add(createFlowRule("/auth/codes", 100));
        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {

        List<DegradeRule> rules = new ArrayList<>();
        rules.add(createDegradeRule("/auth/login", 5, 10000, 3));
        DegradeRuleManager.loadRules(rules);
    }

    private FlowRule createFlowRule(String resource, int qps) {

        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        return rule;
    }

    /**
     * @param resource     资源名
     * @param maxSlowRatio 慢调用比例阈值（秒）
     * @param timeWindow   熔断时长（毫秒）
     * @param minRequest   最小请求数
     */
    private DegradeRule createDegradeRule(String resource, int maxSlowRatio, int timeWindow, int minRequest) {

        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        rule.setCount(maxSlowRatio);
        rule.setTimeWindow(timeWindow / 1000);
        rule.setMinRequestAmount(minRequest);
        rule.setStatIntervalMs(10000);
        return rule;
    }

}
