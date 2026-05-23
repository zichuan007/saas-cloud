package com.saas.cloud.rbac.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.dto.RegisterDTO;
import com.saas.cloud.rbac.api.vo.RegisterVO;
import com.saas.cloud.rbac.service.IAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthController {

    private final IAuthService authService;

    /**
     * 租户注册
     * <p>注册时自动创建租户 + 管理员用户 + 默认角色 + 根部门，返回Token直接登录</p>
     *
     * @param dto 注册请求
     * @return 注册结果（含Token）
     */
    @Operation(summary = "租户注册")
    @PostMapping("/register")
    @com.saas.cloud.common.redis.idempotent.Idempotent(key = "'register:' + #dto.phone", timeout = 10)
    public ApiResult<RegisterVO> register(@Valid @RequestBody RegisterDTO dto) {
        return ApiResult.ok(authService.register(dto));
    }

    /**
     * 用户登录
     *
     * @param params 包含 username, password, tenantCode
     * @return 登录结果（token + 用户信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String tenantCode = (String) params.get("tenantCode");
        return ApiResult.ok(authService.login(username, password, tenantCode));
    }

    /**
     * 刷新令牌
     *
     * @param params 包含 refreshToken
     * @return 新的 token 对
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public ApiResult<Map<String, Object>> refresh(@RequestBody Map<String, Object> params) {
        String refreshToken = (String) params.get("refreshToken");
        return ApiResult.ok(authService.refreshToken(refreshToken));
    }

    /**
     * 登出
     *
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return ApiResult.ok();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/user-info")
    public ApiResult<Map<String, Object>> getUserInfo() {
        Long userId = UserContext.getUserId();
        return ApiResult.ok(authService.getUserInfo(userId));
    }

    /**
     * 获取当前用户权限码列表
     *
     * @return 权限码
     */
    @Operation(summary = "获取当前用户权限码列表")
    @GetMapping("/codes")
    public ApiResult<List<String>> getCodes() {
        Long userId = UserContext.getUserId();
        return ApiResult.ok(authService.getPermissionCodes(userId));
    }
}
