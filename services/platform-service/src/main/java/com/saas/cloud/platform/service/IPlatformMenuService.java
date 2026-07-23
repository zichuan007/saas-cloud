package com.saas.cloud.platform.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.platform.api.dto.PlatformMenuCreateDTO;
import com.saas.cloud.platform.api.dto.PlatformMenuUpdateDTO;
import com.saas.cloud.platform.api.vo.PlatformMenuTreeVO;
import com.saas.cloud.platform.entity.PlatformMenu;

/**
 * 平台菜单 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
public interface IPlatformMenuService extends IService<PlatformMenu> {

    /**
     * 构建菜单树形结构（全量启用菜单）
     *
     * @return 菜单树列表
     */
    List<PlatformMenuTreeVO> buildMenuTree();

    /**
     * 构建 Vben Admin 路由格式（供前端 accessMode=backend 使用）
     *
     * @return Vben 路由配置列表
     */
    List<Map<String, Object>> buildVbenRoutes();

    /**
     * 创建菜单
     *
     * @param dto 创建请求
     */
    void createMenu(PlatformMenuCreateDTO dto);

    /**
     * 更新菜单
     *
     * @param dto 更新请求
     */
    void updateMenu(PlatformMenuUpdateDTO dto);

    /**
     * 删除菜单，有子菜单时禁止删除
     *
     * @param menuId 菜单ID
     */
    void deleteMenu(Long menuId);
}
