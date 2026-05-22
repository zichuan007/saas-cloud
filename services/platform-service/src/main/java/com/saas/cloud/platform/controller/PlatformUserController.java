package com.saas.cloud.platform.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.platform.api.dto.PlatformLoginDTO;
import com.saas.cloud.platform.api.vo.PlatformUserVO;
import com.saas.cloud.platform.service.IPlatformUserService;

import lombok.RequiredArgsConstructor;

/**
 * 平台用户控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PlatformUserController {

    private final IPlatformUserService platformUserService;

    /**
     * 平台管理员登录
     *
     * @param dto 登录请求
     * @return 包含 Token 信息的登录结果
     */
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@Validated @RequestBody PlatformLoginDTO dto) {
        Map<String, Object> result = platformUserService.platformLogin(dto.getUsername(), dto.getPassword());
        return ApiResult.ok(result);
    }

    /**
     * 平台管理员登出
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        // 当前阶段仅清除用户上下文，后续可扩展 Token 黑名单
        UserContext.clear();
        return ApiResult.ok();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/user-info")
    public ApiResult<PlatformUserVO> getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        PlatformUserVO userVO = platformUserService.getCurrentUser(userId);
        return ApiResult.ok(userVO);
    }
}
