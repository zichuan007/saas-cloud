package com.saas.cloud.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.vo.TenantVO;

/**
 * 平台服务 Feign 客户端接口
 * <p>供其他微服务调用平台服务的内部接口</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@FeignClient(value = "platform-service", contextId = "platformFeignClient")
public interface PlatformFeignClient {

    /**
     * 配额校验
     * <p>调用方自行统计当前使用量传入，平台服务负责对比套餐限额</p>
     *
     * @param tenantId     租户ID
     * @param quotaType    配额类型（USER/ROLE/DEPT/PROCESS/WECHAT_ACCOUNT/STORAGE）
     * @param currentCount 当前已使用数量
     * @return 是否在配额内
     */
    @GetMapping("/internal/quota/check")
    ApiResult<Boolean> checkQuota(@RequestParam("tenantId") Long tenantId,
                                  @RequestParam("quotaType") String quotaType,
                                  @RequestParam("currentCount") Integer currentCount);

    /**
     * 获取租户信息
     *
     * @param tenantId 租户ID
     * @return 租户信息
     */
    @GetMapping("/internal/tenant/{id}")
    ApiResult<TenantVO> getTenantInfo(@PathVariable("id") Long tenantId);

    /**
     * 根据租户编码获取租户信息
     *
     * @param tenantCode 租户编码
     * @return 租户信息
     */
    @GetMapping("/internal/tenant/by-code")
    ApiResult<TenantVO> getTenantByCode(@RequestParam("tenantCode") String tenantCode);

    /**
     * 创建租户（内部接口，供注册流程调用）
     *
     * @param dto 租户创建请求
     * @return 创建后的租户信息
     */
    @PostMapping("/internal/tenant/create")
    ApiResult<TenantVO> createTenant(@RequestBody TenantCreateDTO dto);

    /**
     * 获取所有启用状态的租户ID列表
     * <p>供 @TenantJob 定时任务遍历租户使用</p>
     *
     * @return 启用租户ID列表
     */
    @GetMapping("/internal/tenant/active-ids")
    ApiResult<java.util.List<Long>> getActiveTenantIds();
}
