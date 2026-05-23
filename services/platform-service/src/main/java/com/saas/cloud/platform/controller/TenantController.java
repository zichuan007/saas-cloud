package com.saas.cloud.platform.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.excel.ExcelUtils;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.dto.TenantQueryDTO;
import com.saas.cloud.platform.api.vo.TenantExportVO;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.ITenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 租户管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "租户管理")
@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantController {

    private final ITenantService tenantService;

    /**
     * 分页查询租户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询租户列表")
    @GetMapping("/list")
    public ApiResult<PageResult<TenantVO>> pageTenants(TenantQueryDTO query) {
        return ApiResult.ok(tenantService.pageTenants(query));
    }

    /**
     * 获取租户详情
     *
     * @param id 租户ID
     * @return 租户实体
     */
    @Operation(summary = "获取租户详情")
    @GetMapping("/{id}")
    public ApiResult<Tenant> getTenantDetail(@PathVariable("id") Long id) {
        return ApiResult.ok(tenantService.getById(id));
    }

    /**
     * 创建租户
     *
     * @param dto 租户创建请求
     * @return 操作结果
     */
    @Operation(summary = "创建租户")
    @OperationLog(module = "租户管理", operation = "创建租户")
    @PostMapping
    @com.saas.cloud.common.redis.idempotent.Idempotent(key = "'tenant:create:' + #dto.tenantName", timeout = 10)
    public ApiResult<Void> createTenant(@Validated @RequestBody TenantCreateDTO dto) {
        tenantService.createTenant(dto);
        return ApiResult.ok();
    }

    /**
     * 冻结租户
     *
     * @param id 租户ID
     * @return 操作结果
     */
    @Operation(summary = "冻结租户")
    @OperationLog(module = "租户管理", operation = "冻结租户")
    @PutMapping("/{id}/freeze")
    public ApiResult<Void> freezeTenant(@PathVariable("id") Long id) {
        tenantService.freezeTenant(id);
        return ApiResult.ok();
    }

    /**
     * 解冻租户
     *
     * @param id 租户ID
     * @return 操作结果
     */
    @Operation(summary = "解冻租户")
    @OperationLog(module = "租户管理", operation = "解冻租户")
    @PutMapping("/{id}/unfreeze")
    public ApiResult<Void> unfreezeTenant(@PathVariable("id") Long id) {
        tenantService.unfreezeTenant(id);
        return ApiResult.ok();
    }

    /**
     * 导出租户列表
     *
     * @param response HTTP 响应
     */
    @Operation(summary = "导出租户列表")
    @OperationLog(module = "租户管理", operation = "导出租户")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<Tenant> tenants = tenantService.list();
        List<TenantExportVO> voList = tenants.stream().map(tenant -> {
            TenantExportVO vo = new TenantExportVO();
            vo.setTenantCode(tenant.getTenantCode());
            vo.setTenantName(tenant.getTenantName());
            vo.setContactPerson(tenant.getContactPerson());
            vo.setContactPhone(tenant.getContactPhone());
            vo.setContactEmail(tenant.getContactEmail());
            vo.setStatusDesc(TenantStatusEnum.of(tenant.getStatus()).getDesc());
            vo.setCreateTime(tenant.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        ExcelUtils.write(response, "租户列表", "租户", TenantExportVO.class, voList);
    }
}
