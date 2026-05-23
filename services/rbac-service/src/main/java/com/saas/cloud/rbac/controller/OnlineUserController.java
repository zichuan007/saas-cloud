package com.saas.cloud.rbac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.vo.OnlineUserVO;
import com.saas.cloud.rbac.service.IOnlineUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 在线用户管理 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Tag(name = "在线用户管理")
@RestController
@RequestMapping("/online-user")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OnlineUserController {

    private final IOnlineUserService onlineUserService;

    /**
     * 查询当前租户在线用户列表
     *
     * @param username 用户名筛选（可选）
     * @return 在线用户列表
     */
    @Operation(summary = "查询在线用户列表")
    @GetMapping("/list")
    public ApiResult<List<OnlineUserVO>> list(@RequestParam(required = false) String username) {
        return ApiResult.ok(onlineUserService.listOnlineUsers(username));
    }

    /**
     * 强制下线指定用户
     *
     * @param tokenValue token值
     * @return 操作结果
     */
    @Operation(summary = "强制下线指定用户")
    @OperationLog(module = "在线用户", operation = "强制下线")
    @DeleteMapping("/{tokenValue}")
    public ApiResult<Void> kickout(@PathVariable String tokenValue) {
        onlineUserService.kickout(tokenValue);
        return ApiResult.ok();
    }
}
