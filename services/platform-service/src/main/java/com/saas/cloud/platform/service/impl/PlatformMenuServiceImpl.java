package com.saas.cloud.platform.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.platform.api.dto.PlatformMenuCreateDTO;
import com.saas.cloud.platform.api.dto.PlatformMenuUpdateDTO;
import com.saas.cloud.platform.api.vo.PlatformMenuTreeVO;
import com.saas.cloud.platform.entity.PlatformMenu;
import com.saas.cloud.platform.mapper.PlatformMenuMapper;
import com.saas.cloud.platform.service.IPlatformMenuService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 平台菜单 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PlatformMenuServiceImpl extends ServiceImpl<PlatformMenuMapper, PlatformMenu>
        implements IPlatformMenuService {

    private static final long ROOT_PARENT_ID = 0L;

    @Override
    public List<PlatformMenuTreeVO> buildMenuTree() {
        LambdaQueryWrapper<PlatformMenu> query = new LambdaQueryWrapper<>();
        query.eq(PlatformMenu::getStatus, (byte) 1)
                .orderByAsc(PlatformMenu::getSortOrder);

        List<PlatformMenu> menuList = this.list(query);
        List<PlatformMenuTreeVO> voList = menuList.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());
        return buildTree(voList);
    }

    @Override
    public List<Map<String, Object>> buildVbenRoutes() {
        List<PlatformMenuTreeVO> tree = buildMenuTree();
        return tree.stream()
                .filter(menu -> menu.getMenuType() != null && menu.getMenuType() <= 1)
                .map(this::convertToVbenRoute)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(PlatformMenuCreateDTO dto) {
        PlatformMenu menu = new PlatformMenu();
        menu.setMenuName(dto.getMenuName());
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : ROOT_PARENT_ID);
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : (byte) 1);
        menu.setIsExternal(dto.getIsExternal() != null ? dto.getIsExternal() : (byte) 0);
        menu.setIsCached(dto.getIsCached() != null ? dto.getIsCached() : (byte) 0);
        menu.setStatus((byte) 1);
        this.save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(PlatformMenuUpdateDTO dto) {
        PlatformMenu menu = this.getById(dto.getId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        menu.setMenuName(dto.getMenuName());
        if (dto.getParentId() != null) {
            menu.setParentId(dto.getParentId());
        }
        menu.setMenuType(dto.getMenuType());
        if (dto.getPath() != null) {
            menu.setPath(dto.getPath());
        }
        if (dto.getComponent() != null) {
            menu.setComponent(dto.getComponent());
        }
        if (dto.getPermission() != null) {
            menu.setPermission(dto.getPermission());
        }
        if (dto.getIcon() != null) {
            menu.setIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            menu.setSortOrder(dto.getSortOrder());
        }
        if (dto.getVisible() != null) {
            menu.setVisible(dto.getVisible());
        }
        if (dto.getIsExternal() != null) {
            menu.setIsExternal(dto.getIsExternal());
        }
        if (dto.getIsCached() != null) {
            menu.setIsCached(dto.getIsCached());
        }
        this.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        PlatformMenu menu = this.getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        long childCount = this.count(new LambdaQueryWrapper<PlatformMenu>()
                .eq(PlatformMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        this.removeById(menuId);
    }

    private PlatformMenuTreeVO convertToTreeVO(PlatformMenu menu) {
        PlatformMenuTreeVO vo = new PlatformMenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getMenuName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPermission(menu.getPermission());
        vo.setMenuType(menu.getMenuType() != null ? menu.getMenuType().intValue() : null);
        vo.setIcon(menu.getIcon());
        vo.setSortOrder(menu.getSortOrder());
        vo.setVisible(menu.getVisible() != null && menu.getVisible() == 1);
        return vo;
    }

    private List<PlatformMenuTreeVO> buildTree(List<PlatformMenuTreeVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }
        Map<Long, List<PlatformMenuTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(PlatformMenuTreeVO::getParentId));
        voList.forEach(vo -> vo.setChildren(parentMap.getOrDefault(vo.getId(), new ArrayList<>())));
        return voList.stream()
                .filter(vo -> ROOT_PARENT_ID == vo.getParentId())
                .collect(Collectors.toList());
    }

    /**
     * 将菜单树节点转换为 Vben Admin 路由格式
     */
    private Map<String, Object> convertToVbenRoute(PlatformMenuTreeVO menu) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("name", menu.getName());
        route.put("path", menu.getPath());

        if (menu.getComponent() != null) {
            route.put("component", menu.getComponent());
        } else if (menu.getParentId() != null && menu.getParentId() == ROOT_PARENT_ID) {
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
                    .filter(child -> child.getMenuType() != null && child.getMenuType() <= 1)
                    .map(this::convertToVbenRoute)
                    .collect(Collectors.toList());
            route.put("children", children);
        }

        return route;
    }

}
