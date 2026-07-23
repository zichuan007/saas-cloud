package com.saas.cloud.platform.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.ITenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 平台统计控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
@Tag(name = "平台统计")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StatisticsController {

    private final ITenantService tenantService;

    /**
     * 平台概览统计数据
     *
     * @return 概览统计
     */
    @Operation(summary = "平台概览统计")
    @GetMapping("/overview")
    public ApiResult<Map<String, Object>> overview() {
        long totalTenants = tenantService.count();

        long activeTenants = tenantService.count(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, (byte) 1));

        long trialTenants = tenantService.count(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, (byte) 0));

        long frozenTenants = tenantService.count(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, (byte) 2));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTenants", totalTenants);
        result.put("activeTenants", activeTenants);
        result.put("trialTenants", trialTenants);
        result.put("frozenTenants", frozenTenants);
        result.put("totalUsers", 0);
        result.put("todayActiveUsers", 0);
        result.put("totalProcessInstances", 0);
        result.put("monthlyProcessInstances", 0);
        return ApiResult.ok(result);
    }
}
