package com.saas.cloud.rbac.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.excel.ExcelUtils;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.dto.UserCreateDTO;
import com.saas.cloud.rbac.api.dto.UserUpdateDTO;
import com.saas.cloud.rbac.api.vo.ImportResultVO;
import com.saas.cloud.rbac.api.vo.UserExportVO;
import com.saas.cloud.rbac.api.vo.UserImportVO;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.api.vo.UserPageVO;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Tag(name = "用户管理")
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
    @Operation(summary = "分页查询用户列表")
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
    @Operation(summary = "获取用户详情")
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
    @Operation(summary = "创建用户")
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
    @Operation(summary = "更新用户")
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
    @Operation(summary = "删除用户")
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
    @Operation(summary = "启用/禁用用户")
    @OperationLog(module = "用户管理", operation = "启用/禁用用户")
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
    @Operation(summary = "重置用户密码")
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
    @Operation(summary = "更新当前用户个人资料")
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
    @Operation(summary = "修改当前用户密码")
    @OperationLog(module = "个人设置", operation = "修改密码")
    @PutMapping("/password")
    public ApiResult<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return ApiResult.ok();
    }

    /**
     * 导出用户列表
     *
     * @param response HTTP 响应
     * @param keyword  搜索关键字（可选）
     */
    @Operation(summary = "导出用户列表")
    @OperationLog(module = "用户管理", operation = "导出用户")
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String keyword) throws IOException {
        List<User> users = userService.listForExport(keyword);
        List<UserExportVO> voList = users.stream().map(user -> {
            UserExportVO vo = new UserExportVO();
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setStatusDesc(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用");
            vo.setCreateTime(user.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        ExcelUtils.write(response, "用户列表", "用户", UserExportVO.class, voList);
    }

    /**
     * 批量导入用户
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @Operation(summary = "批量导入用户")
    @OperationLog(module = "用户管理", operation = "导入用户")
    @PostMapping("/import")
    public ApiResult<ImportResultVO> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        List<UserImportVO> dataList = ExcelUtils.read(file.getInputStream(), UserImportVO.class);
        int total = dataList.size();
        int success = 0;
        int fail = 0;
        List<String> errorDetails = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            UserImportVO row = dataList.get(i);
            int rowNum = i + 2;
            try {
                if (row.getUsername() == null || row.getUsername().isBlank()) {
                    throw new IllegalArgumentException("用户名不能为空");
                }
                if (row.getPassword() == null || row.getPassword().isBlank()) {
                    throw new IllegalArgumentException("密码不能为空");
                }
                UserCreateDTO dto = new UserCreateDTO();
                dto.setUsername(row.getUsername().trim());
                dto.setPassword(row.getPassword().trim());
                dto.setRealName(row.getRealName());
                dto.setPhone(row.getPhone());
                dto.setEmail(row.getEmail());
                dto.setDeptId(row.getDeptId());
                userService.createUser(dto);
                success++;
            } catch (Exception e) {
                fail++;
                errorDetails.add("第" + rowNum + "行: " + e.getMessage());
            }
        }
        return ApiResult.ok(ImportResultVO.of(total, success, fail, errorDetails));
    }
}
