package com.saas.cloud.platform.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.entity.Order;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.IOrderService;
import com.saas.cloud.platform.service.IPackageService;
import com.saas.cloud.platform.service.ITenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 平台运营 Dashboard 控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Tag(name = "运营报表")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DashboardController {

    private final ITenantService tenantService;
    private final IPackageService packageService;
    private final IOrderService orderService;

    /**
     * 概览数据
     *
     * @return 汇总数据
     */
    @Operation(summary = "概览数据")
    @GetMapping("/overview")
    public ApiResult<Map<String, Object>> overview() {
        long totalTenants = tenantService.count();

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newTenantsThisMonth = tenantService.count(new LambdaQueryWrapper<Tenant>()
                .ge(Tenant::getCreateTime, monthStart));

        long activeTenants = tenantService.count(new LambdaQueryWrapper<Tenant>()
                .in(Tenant::getStatus, (byte) 0, (byte) 1));

        BigDecimal totalRevenue = orderService.lambdaQuery()
                .eq(Order::getPayStatus, 1)
                .list()
                .stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTenants", totalTenants);
        result.put("newTenantsThisMonth", newTenantsThisMonth);
        result.put("activeTenants", activeTenants);
        result.put("totalRevenue", totalRevenue);
        return ApiResult.ok(result);
    }

    /**
     * 租户注册趋势
     *
     * @param days 统计天数
     * @return 按日分组的新增租户数
     */
    @Operation(summary = "租户注册趋势")
    @GetMapping("/tenant-trend")
    public ApiResult<List<Map<String, Object>>> tenantTrend(@RequestParam(value = "days", defaultValue = "30") int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Tenant> tenants = tenantService.list(new LambdaQueryWrapper<Tenant>()
                .ge(Tenant::getCreateTime, startDate.atStartOfDay()));

        Map<LocalDate, Long> countByDate = tenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("count", countByDate.getOrDefault(date, 0L));
            result.add(item);
        }
        return ApiResult.ok(result);
    }

    /**
     * 套餐分布
     *
     * @return 各套餐的租户数量
     */
    @Operation(summary = "套餐分布")
    @GetMapping("/package-distribution")
    public ApiResult<List<Map<String, Object>>> packageDistribution() {
        List<Package> packages = packageService.list();
        Map<Long, String> packageNameMap = packages.stream()
                .collect(Collectors.toMap(Package::getId, Package::getPackageName));

        Map<Long, Long> distribution = tenantService.list().stream()
                .filter(t -> t.getPackageId() != null)
                .collect(Collectors.groupingBy(Tenant::getPackageId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : distribution.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("packageName", packageNameMap.getOrDefault(entry.getKey(), "未知套餐"));
            item.put("count", entry.getValue());
            result.add(item);
        }
        return ApiResult.ok(result);
    }

    /**
     * 活跃度 TOP 租户
     *
     * @param limit 返回数量
     * @return 按用户数排序的租户列表
     */
    @Operation(summary = "活跃度TOP租户")
    @GetMapping("/top-tenants")
    public ApiResult<List<Map<String, Object>>> topTenants(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Tenant> tenants = tenantService.lambdaQuery()
                .in(Tenant::getStatus, (byte) 0, (byte) 1)
                .orderByDesc(Tenant::getCreateTime)
                .last("LIMIT " + limit)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tenant t : tenants) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tenantId", t.getId());
            item.put("tenantName", t.getTenantName());
            item.put("status", t.getStatus());
            item.put("createTime", t.getCreateTime());
            Package pkg = t.getPackageId() != null ? packageService.getById(t.getPackageId()) : null;
            item.put("packageName", pkg != null ? pkg.getPackageName() : "无");
            result.add(item);
        }
        return ApiResult.ok(result);
    }
}
