package com.saas.cloud.rbac.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.dto.UserCreateDTO;
import com.saas.cloud.rbac.api.dto.UserUpdateDTO;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.api.vo.UserPageVO;
import com.saas.cloud.rbac.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 用户管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码，默认1
     * @param pageSize 每页大小，默认10
     * @param keyword  搜索关键字（用户名/真实姓名）
     * @return 用户分页数据
     */
    @GetMapping("/list")
    public ApiResult<PageResult<UserPageVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResult.ok(userService.pageUsers(pageNum, pageSize, keyword));
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情信息
     */
    @GetMapping("/{id}")
    public ApiResult<UserInfoVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(userService.getUserDetail(id));
    }

    /**
     * 创建用户
     *
     * @param dto 用户创建请求
     * @return 操作结果
     */
    @OperationLog(module = "用户管理", operation = "创建用户")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return ApiResult.ok();
    }

    /**
     * 更新用户
     *
     * @param id  用户ID
     * @param dto 用户更新请求
     * @return 操作结果
     */
    @OperationLog(module = "用户管理", operation = "更新用户")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDTO dto) {
        dto.setId(id);
        userService.updateUser(dto);
        return ApiResult.ok();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @OperationLog(module = "用户管理", operation = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ApiResult.ok();
    }

    /**
     * 启用/禁用用户
     *
     * @param id     用户ID
     * @param params 包含 status 字段
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> params) {
        Byte status = Byte.valueOf(String.valueOf(params.get("status")));
        userService.updateStatus(id, status);
        return ApiResult.ok();
    }

    /**
     * 重置用户密码
     *
     * @param id     用户ID
     * @param params 包含 newPassword 字段
     * @return 操作结果
     */
    @OperationLog(module = "用户管理", operation = "重置密码")
    @PutMapping("/{id}/reset-password")
    public ApiResult<Void> resetPassword(@PathVariable("id") Long id,
                                         @RequestBody Map<String, String> params) {
        String newPassword = params.get("newPassword");
        userService.resetPassword(id, newPassword);
        return ApiResult.ok();
    }

    /**
     * 更新当前用户个人资料
     *
     * @param params 包含 realName、phone 字段
     * @return 操作结果
     */
    @OperationLog(module = "个人设置", operation = "修改个人资料")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.updateProfile(userId, params.get("realName"), params.get("phone"));
        return ApiResult.ok();
    }

    /**
     * 修改当前用户密码
     *
     * @param params 包含 oldPassword、newPassword 字段
     * @return 操作结果
     */
    @OperationLog(module = "个人设置", operation = "修改密码")
    @PutMapping("/password")
    public ApiResult<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return ApiResult.ok();
    }
}
