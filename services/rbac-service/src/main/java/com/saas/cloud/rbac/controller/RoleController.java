package com.saas.cloud.rbac.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.RoleCreateDTO;
import com.saas.cloud.rbac.api.dto.RoleUpdateDTO;
import com.saas.cloud.rbac.api.vo.RoleVO;
import com.saas.cloud.rbac.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
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
    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/data-scope")
    public ApiResult<Void> updateDataScope(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> params) {
        Byte dataScope = Byte.valueOf(String.valueOf(params.get("dataScope")));
        List<Long> deptIds = (List<Long>) params.get("deptIds");
        roleService.updateDataScope(id, dataScope, deptIds);
        return ApiResult.ok();
    }
}
