package com.saas.cloud.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.annotation.InnerApi;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.IPackageService;
import com.saas.cloud.platform.service.ITenantService;
import com.saas.cloud.platform.service.impl.TenantCacheService;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 内部接口控制器（供其他微服务通过 Feign 调用）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "平台内部接口")
@Slf4j
@InnerApi
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InternalController {

    private final ITenantService tenantService;
    private final IPackageService packageService;
    private final TenantCacheService tenantCacheService;

    /**
     * 配额校验
     * <p>根据 quotaType 和调用方传入的当前使用量，对比套餐限额判断是否可以继续创建。</p>
     * <p>限额为 0 表示不限制；currentCount >= 限额则返回 false。</p>
     *
     * @param tenantId     租户ID
     * @param quotaType    配额类型（USER/ROLE/DEPT/PROCESS/WECHAT_ACCOUNT/STORAGE）
     * @param currentCount 当前已使用数量
     * @return 是否在配额内
     */
    @Operation(summary = "配额校验")
    @GetMapping("/quota/check")
    public ApiResult<Boolean> checkQuota(@RequestParam("tenantId") Long tenantId,
                                         @RequestParam("quotaType") String quotaType,
                                         @RequestParam("currentCount") Integer currentCount) {
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "租户不存在, tenantId=" + tenantId);
        }

        // 校验租户状态
        if (tenant.getStatus() == (byte) TenantStatusEnum.FROZEN.getCode()) {
            log.warn("配额校验失败: 租户已冻结, tenantId={}", tenantId);
            return ApiResult.ok(false);
        }
        if (tenant.getStatus() == (byte) TenantStatusEnum.DEACTIVATED.getCode()) {
            log.warn("配额校验失败: 租户已注销, tenantId={}", tenantId);
            return ApiResult.ok(false);
        }

        // 查询租户关联的套餐
        if (tenant.getPackageId() == null) {
            log.warn("租户未关联套餐, 默认放行, tenantId={}", tenantId);
            return ApiResult.ok(true);
        }
        Package pkg = packageService.getById(tenant.getPackageId());
        if (pkg == null) {
            log.warn("套餐不存在, 默认放行, tenantId={}, packageId={}", tenantId, tenant.getPackageId());
            return ApiResult.ok(true);
        }

        // 根据 quotaType 获取对应限额
        Integer maxLimit = getMaxLimitByType(pkg, quotaType);
        if (maxLimit == null) {
            log.warn("不支持的配额类型, quotaType={}", quotaType);
            return ApiResult.ok(true);
        }

        // 限额为 0 表示不限制
        if (maxLimit == 0) {
            log.debug("配额不限制, tenantId={}, quotaType={}", tenantId, quotaType);
            return ApiResult.ok(true);
        }

        // 当前使用量 >= 限额，则不允许继续创建
        if (currentCount >= maxLimit) {
            log.warn("配额已达上限, tenantId={}, quotaType={}, currentCount={}, maxLimit={}",
                    tenantId, quotaType, currentCount, maxLimit);
            return ApiResult.ok(false);
        }

        log.debug("配额校验通过, tenantId={}, quotaType={}, currentCount={}, maxLimit={}",
                tenantId, quotaType, currentCount, maxLimit);
        return ApiResult.ok(true);
    }

    /**
     * 根据配额类型获取套餐中对应的限额值
     *
     * @param pkg       套餐实体
     * @param quotaType 配额类型
     * @return 限额值，不支持的类型返回 null
     */
    private Integer getMaxLimitByType(Package pkg, String quotaType) {
        switch (quotaType.toUpperCase()) {
            case "USER":
                return pkg.getMaxUsers();
            case "ROLE":
                return pkg.getMaxRoles();
            case "DEPT":
                return pkg.getMaxDepts();
            case "PROCESS":
                return pkg.getMaxProcessDefinitions();
            case "WECHAT_ACCOUNT":
                return pkg.getMaxWechatAccounts();
            default:
                return null;
        }
    }

    /**
     * 根据租户编码获取租户信息
     *
     * @param tenantCode 租户编码
     * @return 租户视图对象
     */
    @Operation(summary = "根据租户编码获取租户信息")
    @GetMapping("/tenant/by-code")
    public ApiResult<TenantVO> getTenantByCode(@RequestParam("tenantCode") String tenantCode) {
        // 优先从 Redis 缓存获取
        TenantVO vo = tenantCacheService.getTenantByCode(tenantCode);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "租户不存在, tenantCode=" + tenantCode);
        }
        return ApiResult.ok(vo);
    }

    /**
     * 创建租户（内部接口，供注册流程远程调用）
     *
     * @param dto 租户创建请求
     * @return 创建后的租户视图对象
     */
    @Operation(summary = "创建租户(内部接口)")
    @PostMapping("/tenant/create")
    public ApiResult<TenantVO> createTenant(@RequestBody TenantCreateDTO dto) {
        Tenant tenant = tenantService.createTenantAndReturn(dto);

        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantCode(tenant.getTenantCode());
        vo.setTenantName(tenant.getTenantName());
        vo.setContactName(tenant.getContactPerson());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setContactEmail(tenant.getContactEmail());
        vo.setStatus(tenant.getStatus().intValue());
        vo.setStatusDesc(TenantStatusEnum.of(tenant.getStatus()).getDesc());
        vo.setCreateTime(tenant.getCreateTime());

        // 套餐信息
        vo.setPackageId(tenant.getPackageId());
        if (tenant.getPackageId() != null) {
            Package pkg = packageService.getById(tenant.getPackageId());
            if (pkg != null) {
                vo.setPackageName(pkg.getPackageName());
                vo.setMaxUsers(pkg.getMaxUsers());
                vo.setMenuIds(pkg.getMenuIds());
            }
        }

        return ApiResult.ok(vo);
    }

    /**
     * 获取所有启用状态的租户ID列表
     * <p>供 @TenantJob 定时任务遍历租户使用</p>
     *
     * @return 启用租户ID列表
     */
    @Operation(summary = "获取启用租户ID列表")
    @GetMapping("/tenant/active-ids")
    public ApiResult<List<Long>> getActiveTenantIds() {
        TenantContext.setIgnoreTenant(true);
        try {
            List<Tenant> tenants = tenantService.lambdaQuery()
                    .select(Tenant::getId)
                    .in(Tenant::getStatus,
                            (byte) TenantStatusEnum.TRIAL.getCode(),
                            (byte) TenantStatusEnum.ACTIVE.getCode())
                    .list();
            List<Long> ids = tenants.stream()
                    .map(Tenant::getId)
                    .collect(Collectors.toList());
            return ApiResult.ok(ids);
        } finally {
            TenantContext.clearIgnoreTenant();
        }
    }

    /**
     * 获取租户信息
     *
     * @param id 租户ID
     * @return 租户视图对象
     */
    @Operation(summary = "获取租户信息")
    @GetMapping("/tenant/{id}")
    public ApiResult<TenantVO> getTenantInfo(@PathVariable("id") Long id) {
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "租户不存在, tenantId=" + id);
        }

        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantCode(tenant.getTenantCode());
        vo.setTenantName(tenant.getTenantName());
        vo.setContactName(tenant.getContactPerson());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setContactEmail(tenant.getContactEmail());
        vo.setStatus(tenant.getStatus().intValue());
        vo.setStatusDesc(TenantStatusEnum.of(tenant.getStatus()).getDesc());
        vo.setCreateTime(tenant.getCreateTime());

        // 根据状态设置过期时间
        if (tenant.getStatus() == (byte) TenantStatusEnum.TRIAL.getCode()) {
            vo.setExpireTime(tenant.getTrialExpireTime());
        } else {
            vo.setExpireTime(tenant.getPaidExpireTime());
        }

        // 套餐信息
        vo.setPackageId(tenant.getPackageId());
        if (tenant.getPackageId() != null) {
            Package pkg = packageService.getById(tenant.getPackageId());
            if (pkg != null) {
                vo.setPackageName(pkg.getPackageName());
                vo.setMaxUsers(pkg.getMaxUsers());
                vo.setMenuIds(pkg.getMenuIds());
            }
        }

        return ApiResult.ok(vo);
    }
}
