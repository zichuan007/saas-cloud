package com.saas.cloud.rbac.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.mapper.DeptMapper;
import com.saas.cloud.rbac.mapper.RoleMapper;
import com.saas.cloud.rbac.mapper.UserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 租户自助中心
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "租户自助中心")
@RestController
@RequestMapping("/tenant-self")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantSelfController {

    private final PlatformFeignClient platformFeignClient;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;

    /**
     * 查看当前租户信息
     *
     * @return 租户信息（含套餐、到期时间）
     */
    @Operation(summary = "查看租户信息")
    @GetMapping("/info")
    public ApiResult<TenantVO> info() {
        Long tenantId = TenantContext.getTenantId();
        ApiResult<TenantVO> result = platformFeignClient.getTenantInfo(tenantId);
        return ApiResult.ok(result != null ? result.getData() : null);
    }

    /**
     * 查看配额使用情况
     *
     * @return 各维度已用/上限数据
     */
    @Operation(summary = "查看配额使用情况")
    @GetMapping("/quota")
    public ApiResult<Map<String, Object>> quota() {
        Long userCount = userMapper.selectCount(null);
        Long roleCount = roleMapper.selectCount(null);
        Long deptCount = deptMapper.selectCount(null);

        Map<String, Object> quota = new LinkedHashMap<>();
        quota.put("userCount", userCount);
        quota.put("roleCount", roleCount);
        quota.put("deptCount", deptCount);
        return ApiResult.ok(quota);
    }
}
