package com.saas.cloud.platform.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.dto.TenantQueryDTO;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.ITenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
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
    @OperationLog(module = "租户管理", operation = "创建租户")
    @PostMapping
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
    @OperationLog(module = "租户管理", operation = "解冻租户")
    @PutMapping("/{id}/unfreeze")
    public ApiResult<Void> unfreezeTenant(@PathVariable("id") Long id) {
        tenantService.unfreezeTenant(id);
        return ApiResult.ok();
    }
}
