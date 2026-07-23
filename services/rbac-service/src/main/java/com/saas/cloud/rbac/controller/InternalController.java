package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.security.annotation.InnerApi;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.entity.Dept;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.service.IDeptService;
import com.saas.cloud.rbac.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RBAC 内部接口控制器（供其他微服务通过 Feign 调用）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Tag(name = "内部接口")
@InnerApi
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InternalController {

    private final IUserService userService;
    private final IDeptService deptService;

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Operation(summary = "根据用户ID获取用户信息")
    @TenantIgnore
    @GetMapping("/user/{id}")
    public ApiResult<UserInfoVO> getUserById(@PathVariable("id") Long id) {
        log.info("内部接口: 获取用户信息, userId={}", id);
        UserInfoVO userInfo = userService.getUserDetail(id);
        return ApiResult.ok(userInfo);
    }

    /**
     * 获取指定租户的用户数量
     *
     * @param tenantId 租户ID
     * @return 用户数量
     */
    @Operation(summary = "获取指定租户的用户数量")
    @TenantIgnore
    @GetMapping("/user/count")
    public ApiResult<Long> getUserCount(@RequestParam("tenantId") Long tenantId) {
        log.info("内部接口: 获取租户用户数, tenantId={}", tenantId);
        long count = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId));
        return ApiResult.ok(count);
    }

    /**
     * 获取部门负责人信息
     *
     * @param id 部门ID
     * @return 部门负责人用户信息
     */
    @Operation(summary = "获取部门负责人信息")
    @TenantIgnore
    @GetMapping("/dept/{id}/leader")
    public ApiResult<UserInfoVO> getDeptLeader(@PathVariable("id") Long id) {
        log.info("内部接口: 获取部门负责人, deptId={}", id);
        Dept dept = deptService.getById(id);
        if (dept == null || dept.getLeaderUserId() == null) {
            log.warn("部门不存在或未设置负责人, deptId={}", id);
            return ApiResult.ok(null);
        }
        UserInfoVO leaderInfo = userService.getUserDetail(dept.getLeaderUserId());
        return ApiResult.ok(leaderInfo);
    }
}
