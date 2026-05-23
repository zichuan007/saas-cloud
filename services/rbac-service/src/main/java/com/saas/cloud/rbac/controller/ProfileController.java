package com.saas.cloud.rbac.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.entity.SocialUser;
import com.saas.cloud.rbac.service.ISocialLoginService;
import com.saas.cloud.rbac.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 个人中心
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProfileController {

    private final IUserService userService;
    private final ISocialLoginService socialLoginService;

    /**
     * 获取当前用户个人信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户个人信息")
    @GetMapping
    public ApiResult<UserInfoVO> info() {
        return ApiResult.ok(userService.getUserDetail(UserContext.getUserId()));
    }

    /**
     * 修改个人资料
     *
     * @param params 包含 realName、phone 字段
     * @return 操作结果
     */
    @Operation(summary = "修改个人资料")
    @OperationLog(module = "个人中心", operation = "修改个人资料")
    @PutMapping
    public ApiResult<Void> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.updateProfile(userId, params.get("realName"), params.get("phone"));
        return ApiResult.ok();
    }

    /**
     * 修改密码
     *
     * @param params 包含 oldPassword、newPassword 字段
     * @return 操作结果
     */
    @Operation(summary = "修改密码")
    @OperationLog(module = "个人中心", operation = "修改密码")
    @PutMapping("/password")
    public ApiResult<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return ApiResult.ok();
    }

    /**
     * 修改头像
     *
     * @param params 包含 avatar 字段（头像URL）
     * @return 操作结果
     */
    @Operation(summary = "修改头像")
    @OperationLog(module = "个人中心", operation = "修改头像")
    @PutMapping("/avatar")
    public ApiResult<Void> updateAvatar(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.updateAvatar(userId, params.get("avatar"));
        return ApiResult.ok();
    }

    /**
     * 查看社交账号绑定列表
     *
     * @return 已绑定的社交账号列表
     */
    @Operation(summary = "查看社交账号绑定")
    @GetMapping("/social")
    public ApiResult<List<SocialUser>> listSocialBindings() {
        return ApiResult.ok(socialLoginService.listBoundSocials(UserContext.getUserId()));
    }
}
