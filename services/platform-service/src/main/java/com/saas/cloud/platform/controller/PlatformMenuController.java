package com.saas.cloud.platform.controller;

import java.util.List;

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
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.dto.PlatformMenuCreateDTO;
import com.saas.cloud.platform.api.dto.PlatformMenuUpdateDTO;
import com.saas.cloud.platform.api.vo.PlatformMenuTreeVO;
import com.saas.cloud.platform.service.IPlatformMenuService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 平台菜单管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
@Tag(name = "平台菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PlatformMenuController {

    private final IPlatformMenuService platformMenuService;

    /**
     * 获取菜单树形列表（全量）
     *
     * @return 菜单树
     */
    @Operation(summary = "获取平台菜单树形列表")
    @GetMapping("/tree")
    public ApiResult<List<PlatformMenuTreeVO>> tree() {
        return ApiResult.ok(platformMenuService.buildMenuTree());
    }

    /**
     * 创建菜单
     *
     * @param dto 菜单创建请求
     * @return 操作结果
     */
    @Operation(summary = "创建平台菜单")
    @OperationLog(module = "平台菜单管理", operation = "创建菜单")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody PlatformMenuCreateDTO dto) {
        platformMenuService.createMenu(dto);
        return ApiResult.ok();
    }

    /**
     * 更新菜单
     *
     * @param id  菜单ID
     * @param dto 菜单更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新平台菜单")
    @OperationLog(module = "平台菜单管理", operation = "更新菜单")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody PlatformMenuUpdateDTO dto) {
        dto.setId(id);
        platformMenuService.updateMenu(dto);
        return ApiResult.ok();
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @Operation(summary = "删除平台菜单")
    @OperationLog(module = "平台菜单管理", operation = "删除菜单")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        platformMenuService.deleteMenu(id);
        return ApiResult.ok();
    }
}
