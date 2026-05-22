package com.saas.cloud.rbac.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.rbac.api.vo.UserInfoVO;

/**
 * 权限管理服务 Feign 客户端接口
 * <p>供其他微服务调用 RBAC 服务的内部接口</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@FeignClient(value = "rbac-service", contextId = "rbacFeignClient")
public interface RbacFeignClient {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/internal/user/{id}")
    ApiResult<UserInfoVO> getUserById(@PathVariable("id") Long userId);

    /**
     * 获取指定租户的用户数量
     *
     * @param tenantId 租户ID
     * @return 用户数量
     */
    @GetMapping("/internal/user/count")
    ApiResult<Long> getUserCount(@RequestParam("tenantId") Long tenantId);

    /**
     * 获取部门负责人信息
     *
     * @param deptId 部门ID
     * @return 部门负责人用户信息
     */
    @GetMapping("/internal/dept/{id}/leader")
    ApiResult<UserInfoVO> getDeptLeader(@PathVariable("id") Long deptId);
}
