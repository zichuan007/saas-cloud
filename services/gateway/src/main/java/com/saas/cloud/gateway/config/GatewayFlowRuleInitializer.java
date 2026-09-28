package com.saas.cloud.gateway.config;

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关层 Route 维度限流规则初始化
 * <p>每个下游服务一条 QPS 总量规则，覆盖该服务全部接口，避免逐接口硬编码。</p>
 * <p>阈值变更应优先走 Sentinel Dashboard / Nacos datasource 动态推送；此处仅作启动兜底默认值。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-09-28
 */
@Slf4j
@Component
public class GatewayFlowRuleInitializer {

    /**
     * 初始化网关限流规则
     */
    @PostConstruct
    public void initRules() {
        Set<GatewayFlowRule> rules = new HashSet<GatewayFlowRule>();
        // Route 维度：resource 名 = 网关路由 id，一条规则覆盖整个服务全部接口
        rules.add(build("rbac-service", 500));
        rules.add(build("platform-service", 300));
        rules.add(build("workflow-service", 200));
        rules.add(build("wechat-oa-service", 200));
        rules.add(build("notify-service", 300));
        GatewayRuleManager.loadRules(rules);
        log.info("[Sentinel-Gateway] Route 维度限流规则加载完成, count={}", rules.size());
    }

    /**
     * 构建单条 Route QPS 限流规则
     *
     * @param routeId 路由 id（对应下游服务）
     * @param qps     每秒请求数阈值
     * @return 网关流控规则
     */
    private GatewayFlowRule build(String routeId, int qps) {
        return new GatewayFlowRule(routeId)
                .setCount(qps)
                .setIntervalSec(1);
    }
}
