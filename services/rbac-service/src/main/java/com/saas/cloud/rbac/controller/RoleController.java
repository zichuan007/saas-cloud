package com.saas.cloud.rbac.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.excel.ExcelUtils;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.RoleCreateDTO;
import com.saas.cloud.rbac.api.dto.RoleUpdateDTO;
import com.saas.cloud.rbac.api.vo.RoleExportVO;
import com.saas.cloud.rbac.api.vo.RoleVO;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.service.IRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 角色管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RoleController {

    private final IRoleService roleService;

    /**
     * 查询角色列表
     *
     * @return 角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public ApiResult<List<RoleVO>> list() {
        return ApiResult.ok(roleService.listRoles());
    }

    /**
     * 创建角色
     *
     * @param dto 角色创建请求
     * @return 操作结果
     */
    @Operation(summary = "创建角色")
    @OperationLog(module = "角色管理", operation = "创建角色")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody RoleCreateDTO dto) {
        roleService.createRole(dto);
        return ApiResult.ok();
    }

    /**
     * 更新角色
     *
     * @param id  角色ID
     * @param dto 角色更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新角色")
    @OperationLog(module = "角色管理", operation = "更新角色")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        dto.setId(id);
        roleService.updateRole(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @OperationLog(module = "角色管理", operation = "删除角色")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return ApiResult.ok();
    }

    /**
     * 分配菜单权限
     *
     * @param id     角色ID
     * @param params 包含 menuIds 字段
     * @return 操作结果
     */
    @Operation(summary = "分配菜单权限")
    @OperationLog(module = "角色管理", operation = "分配菜单")
    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/menus")
    public ApiResult<Void> assignMenus(@PathVariable("id") Long id,
                                       @RequestBody Map<String, List<Long>> params) {
        List<Long> menuIds = params.get("menuIds");
        roleService.assignMenus(id, menuIds);
        return ApiResult.ok();
    }

    /**
     * 设置数据范围
     *
     * @param id     角色ID
     * @param params 包含 dataScope 和 deptIds 字段
     * @return 操作结果
     */
    @Operation(summary = "设置数据范围")
    @OperationLog(module = "角色管理", operation = "设置数据范围")
    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/data-scope")
    public ApiResult<Void> updateDataScope(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> params) {
        Byte dataScope = Byte.valueOf(String.valueOf(params.get("dataScope")));
        List<Long> deptIds = (List<Long>) params.get("deptIds");
        roleService.updateDataScope(id, dataScope, deptIds);
        return ApiResult.ok();
    }

    /**
     * 导出角色列表
     *
     * @param response HTTP 响应
     */
    @Operation(summary = "导出角色列表")
    @OperationLog(module = "角色管理", operation = "导出角色")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<Role> roles = roleService.list();
        List<RoleExportVO> voList = roles.stream().map(role -> {
            RoleExportVO vo = new RoleExportVO();
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleLevelDesc(roleLevelDesc(role.getRoleLevel()));
            vo.setDataScopeDesc(dataScopeDesc(role.getDataScope()));
            vo.setStatusDesc(role.getStatus() != null && role.getStatus() == 1 ? "启用" : "禁用");
            vo.setCreateTime(role.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        ExcelUtils.write(response, "角色列表", "角色", RoleExportVO.class, voList);
    }

    private String roleLevelDesc(Byte level) {
        if (level == null) return "未知";
        switch (level) {
            case 0: return "超管";
            case 1: return "管理员";
            case 2: return "普通";
            default: return "未知";
        }
    }

    private String dataScopeDesc(Byte scope) {
        if (scope == null) return "未知";
        switch (scope) {
            case 1: return "全部";
            case 2: return "本部门及下级";
            case 3: return "本部门";
            case 4: return "仅本人";
            case 5: return "自定义";
            default: return "未知";
        }
    }
}
