package com.saas.cloud.rbac.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.rbac.api.dto.MenuCreateDTO;
import com.saas.cloud.rbac.api.dto.MenuUpdateDTO;
import com.saas.cloud.rbac.api.vo.MenuTreeVO;
import com.saas.cloud.rbac.entity.Menu;

/**
 * 菜单表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IMenuService extends IService<Menu> {

    /**
     * 构建菜单树形结构（按当前租户套餐过滤可见菜单）
     *
     * @return 菜单树列表
     */
    List<MenuTreeVO> buildMenuTree();

    /**
     * 根据用户ID获取其有权限的菜单树
     *
     * @param userId 用户ID
     * @return 菜单树列表（仅包含目录和菜单，不含按钮）
     */
    List<MenuTreeVO> getMenusByUserId(Long userId);

    /**
     * 创建菜单
     *
     * @param dto 菜单创建请求
     */
    void createMenu(MenuCreateDTO dto);

    /**
     * 更新菜单
     *
     * @param dto 菜单更新请求
     */
    void updateMenu(MenuUpdateDTO dto);

    /**
     * 删除菜单，有子菜单时禁止删除
     *
     * @param menuId 菜单ID
     */
    void deleteMenu(Long menuId);
}
