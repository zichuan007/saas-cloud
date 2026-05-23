package com.saas.cloud.rbac.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.entity.SocialUser;
import com.saas.cloud.rbac.service.ISocialLoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 社交登录
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "社交登录")
@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SocialLoginController {

    private final ISocialLoginService socialLoginService;

    /**
     * 获取第三方平台授权URL
     *
     * @param type       平台类型（wechat/dingtalk/github/gitee）
     * @param tenantCode 租户编码
     * @return 授权URL
     */
    @Operation(summary = "获取社交登录授权URL")
    @GetMapping("/{type}")
    public ApiResult<String> getAuthorizeUrl(@PathVariable String type,
                                             @RequestParam String tenantCode) {
        return ApiResult.ok(socialLoginService.getAuthorizeUrl(type, tenantCode));
    }

    /**
     * 第三方登录回调
     *
     * @param type       平台类型
     * @param tenantCode 租户编码
     * @param code       授权码
     * @param state      状态参数
     * @return 登录/绑定结果
     */
    @Operation(summary = "社交登录回调")
    @GetMapping("/{type}/callback")
    public ApiResult<Map<String, Object>> callback(@PathVariable String type,
                                                   @RequestParam String tenantCode,
                                                   @RequestParam String code,
                                                   @RequestParam String state) {
        return ApiResult.ok(socialLoginService.callback(type, tenantCode, code, state));
    }

    /**
     * 绑定社交账号到当前用户
     *
     * @param params 包含 socialType/socialId/socialName/socialAvatar
     * @return 操作结果
     */
    @Operation(summary = "绑定社交账号")
    @OperationLog(module = "社交登录", operation = "绑定账号")
    @PostMapping("/bind")
    public ApiResult<Void> bind(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        socialLoginService.bindSocial(userId,
                params.get("socialType"),
                params.get("socialId"),
                params.get("socialName"),
                params.get("socialAvatar"));
        return ApiResult.ok();
    }

    /**
     * 解绑社交账号
     *
     * @param type 平台类型
     * @return 操作结果
     */
    @Operation(summary = "解绑社交账号")
    @OperationLog(module = "社交登录", operation = "解绑账号")
    @DeleteMapping("/unbind/{type}")
    public ApiResult<Void> unbind(@PathVariable String type) {
        Long userId = UserContext.getUserId();
        socialLoginService.unbindSocial(userId, type);
        return ApiResult.ok();
    }

    /**
     * 查询已绑定的社交账号
     *
     * @return 已绑定的社交账号列表
     */
    @Operation(summary = "查询已绑定的社交账号")
    @GetMapping("/bound")
    public ApiResult<List<SocialUser>> listBound() {
        Long userId = UserContext.getUserId();
        return ApiResult.ok(socialLoginService.listBoundSocials(userId));
    }
}
