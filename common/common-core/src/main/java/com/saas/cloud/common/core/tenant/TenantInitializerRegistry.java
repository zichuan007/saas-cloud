package com.saas.cloud.common.core.tenant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户初始化器注册表
 * 收集所有 {@link TenantInitializer} Bean，按 order 排序后依次执行。
 * 任意一步失败时，倒序调用已成功初始化器的 rollback 方法。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
public class TenantInitializerRegistry {

    @Autowired(required = false)
    private List<TenantInitializer> initializers;

    @PostConstruct
    public void init() {
        if (initializers == null) {
            initializers = new ArrayList<>();
        }
        initializers.sort(Comparator.comparingInt(TenantInitializer::getOrder));
        log.info("租户初始化器注册完成, 共 {} 个: {}",
                initializers.size(),
                initializers.stream()
                        .map(i -> i.getCode() + "(order=" + i.getOrder() + ")")
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("无"));
    }

    /**
     * 执行所有初始化器
     *
     * @param context 租户初始化上下文
     */
    public void initialize(TenantInitContext context) {
        List<TenantInitializer> completed = new ArrayList<>();
        for (TenantInitializer initializer : initializers) {
            try {
                log.info("执行租户初始化器: {} (order={})", initializer.getCode(), initializer.getOrder());
                initializer.initialize(context);
                completed.add(initializer);
            } catch (Exception e) {
                log.error("租户初始化器 {} 执行失败, 开始回滚", initializer.getCode(), e);
                rollbackInOrder(completed, context);
                throw new RuntimeException("租户初始化失败: " + initializer.getCode() + " - " + e.getMessage(), e);
            }
        }
        log.info("租户初始化完成, tenantId={}", context.getTenantId());
    }

    /**
     * 倒序回滚已成功的初始化器
     */
    private void rollbackInOrder(List<TenantInitializer> completed, TenantInitContext context) {
        for (int i = completed.size() - 1; i >= 0; i--) {
            TenantInitializer initializer = completed.get(i);
            try {
                log.info("回滚租户初始化器: {}", initializer.getCode());
                initializer.rollback(context);
            } catch (Exception rollbackEx) {
                log.error("回滚租户初始化器 {} 失败", initializer.getCode(), rollbackEx);
            }
        }
    }
}
