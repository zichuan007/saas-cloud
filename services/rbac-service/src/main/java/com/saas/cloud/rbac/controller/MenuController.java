package com.saas.cloud.rbac.controller;

import java.util.LinkedHashMap;
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
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.dto.MenuCreateDTO;
import com.saas.cloud.rbac.api.dto.MenuUpdateDTO;
import com.saas.cloud.rbac.api.vo.MenuTreeVO;
import com.saas.cloud.rbac.service.IMenuService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 菜单管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MenuController {

    private final IMenuService menuService;

    /**
     * 获取菜单树形列表（全量）
     *
     * @return 菜单树
     */
    @GetMapping("/tree")
    public ApiResult<List<MenuTreeVO>> tree() {
        Long tenantId = TenantContext.getTenantId();
        return ApiResult.ok(menuService.buildMenuTree(tenantId));
    }

    /**
     * 获取当前用户有权限的菜单树
     *
     * @return 用户菜单树（仅目录和菜单，不含按钮）
     */
    @GetMapping("/user-menus")
    public ApiResult<List<MenuTreeVO>> userMenus() {
        Long userId = UserContext.getUserId();
        return ApiResult.ok(menuService.getMenusByUserId(userId));
    }

    /**
     * 获取当前用户菜单树（Vben Admin RouteRecord 格式）
     *
     * @return Vben 兼容的路由配置列表
     */
    @GetMapping("/user-tree")
    public ApiResult<List<Map<String, Object>>> userTree() {
        Long userId = UserContext.getUserId();
        List<MenuTreeVO> menuTree = menuService.getMenusByUserId(userId);
        List<Map<String, Object>> routeList = menuTree.stream()
                .map(this::convertToVbenRoute)
                .collect(Collectors.toList());
        return ApiResult.ok(routeList);
    }

    /**
     * 将 MenuTreeVO 转换为 Vben Admin 路由格式
     */
    private Map<String, Object> convertToVbenRoute(MenuTreeVO menu) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("name", menu.getName());
        route.put("path", menu.getPath());

        if (menu.getComponent() != null) {
            route.put("component", menu.getComponent());
        } else if (menu.getParentId() != null && menu.getParentId() == 0L) {
            route.put("component", "BasicLayout");
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", menu.getName());
        if (menu.getIcon() != null && !menu.getIcon().isEmpty()) {
            meta.put("icon", menu.getIcon());
        }
        meta.put("order", menu.getSortOrder());
        if (menu.getVisible() != null && !menu.getVisible()) {
            meta.put("hideInMenu", true);
        }
        route.put("meta", meta);

        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<Map<String, Object>> children = menu.getChildren().stream()
                    .map(this::convertToVbenRoute)
                    .collect(Collectors.toList());
            route.put("children", children);
        }

        return route;
    }

    /**
     * 创建菜单
     *
     * @param dto 菜单创建请求
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody MenuCreateDTO dto) {
        menuService.createMenu(dto);
        return ApiResult.ok();
    }

    /**
     * 更新菜单
     *
     * @param id  菜单ID
     * @param dto 菜单更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody MenuUpdateDTO dto) {
        dto.setId(id);
        menuService.updateMenu(dto);
        return ApiResult.ok();
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        menuService.deleteMenu(id);
        return ApiResult.ok();
    }
}
