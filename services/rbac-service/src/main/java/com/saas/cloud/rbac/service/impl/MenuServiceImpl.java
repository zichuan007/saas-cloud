package com.saas.cloud.rbac.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.api.dto.MenuCreateDTO;
import com.saas.cloud.rbac.api.dto.MenuUpdateDTO;
import com.saas.cloud.rbac.api.vo.MenuTreeVO;
import com.saas.cloud.rbac.entity.Menu;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.mapper.MenuMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.service.IMenuService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 菜单表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    private final UserMapper userMapper;
    private final PlatformFeignClient platformFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 根节点的 parentId
     */
    private static final long ROOT_PARENT_ID = 0L;

    @Override
    public List<MenuTreeVO> buildMenuTree(Long tenantId) {
        log.info("构建菜单树, tenantId={}", tenantId);
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getStatus, (byte) 1)
                .orderByAsc(Menu::getSortOrder);

        // 按套餐过滤可见菜单
        Set<Long> allowedMenuIds = loadPackageMenuIds(tenantId);
        if (allowedMenuIds != null) {
            queryWrapper.in(Menu::getId, allowedMenuIds);
        }

        List<Menu> menuList = this.list(queryWrapper);
        List<MenuTreeVO> voList = menuList.stream()
                .map(this::convertToMenuTreeVO)
                .collect(Collectors.toList());
        return buildTree(voList);
    }

    @Override
    public List<MenuTreeVO> getMenusByUserId(Long userId) {
        log.info("获取用户菜单树, userId={}", userId);

        User user = userMapper.selectById(userId);
        List<Menu> menuList;

        if (user != null && user.getRoleLevel() != null && user.getRoleLevel() == 0) {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Menu::getMenuType, (byte) 0, (byte) 1)
                    .eq(Menu::getStatus, (byte) 1)
                    .orderByAsc(Menu::getSortOrder);
            menuList = this.list(queryWrapper);
        } else {
            menuList = baseMapper.selectMenusByUserId(userId);
        }

        // 按套餐过滤可见菜单（超管也受套餐限制）
        Set<Long> allowedMenuIds = (user != null) ? loadPackageMenuIds(user.getTenantId()) : null;

        List<MenuTreeVO> voList = menuList.stream()
                .filter(menu -> menu.getMenuType() == null || menu.getMenuType() != 2)
                .filter(menu -> allowedMenuIds == null || allowedMenuIds.contains(menu.getId()))
                .map(this::convertToMenuTreeVO)
                .collect(Collectors.toList());
        return buildTree(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(MenuCreateDTO dto) {
        log.info("创建菜单, menuName={}, parentId={}", dto.getMenuName(), dto.getParentId());

        Menu menu = new Menu();
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
        menu.setModule(dto.getModule());
        menu.setStatus((byte) 1);

        this.save(menu);
        log.info("菜单创建成功, id={}", menu.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuUpdateDTO dto) {
        log.info("更新菜单, id={}", dto.getId());
        Menu menu = this.getById(dto.getId());
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
        if (dto.getModule() != null) {
            menu.setModule(dto.getModule());
        }

        this.updateById(menu);
        log.info("菜单更新成功, id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        log.info("删除菜单, menuId={}", menuId);
        Menu menu = this.getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        long childCount = this.count(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }

        this.removeById(menuId);
        log.info("菜单删除成功, menuId={}", menuId);
    }

    /**
     * 将菜单实体转换为树形VO
     *
     * @param menu 菜单实体
     * @return 菜单树VO
     */
    private MenuTreeVO convertToMenuTreeVO(Menu menu) {
        MenuTreeVO vo = new MenuTreeVO();
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

    /**
     * 加载租户套餐的可见菜单ID集合
     *
     * @param tenantId 租户ID
     * @return 可见菜单ID集合，null 表示不限制（旗舰版或未配置套餐）
     */
    private Set<Long> loadPackageMenuIds(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        try {
            ApiResult<TenantVO> result = platformFeignClient.getTenantInfo(tenantId);
            if (result == null || result.getData() == null) {
                return null;
            }
            String menuIdsJson = result.getData().getMenuIds();
            if (!StringUtils.hasText(menuIdsJson)) {
                return null;
            }
            List<Long> menuIdList = objectMapper.readValue(menuIdsJson, new TypeReference<List<Long>>() {});
            if (CollectionUtils.isEmpty(menuIdList)) {
                return null;
            }
            return new HashSet<>(menuIdList);
        } catch (Exception e) {
            log.warn("加载套餐菜单失败, 降级返回全部菜单, tenantId={}, error={}", tenantId, e.getMessage());
            return null;
        }
    }

    /**
     * 将平铺的 VO 列表组装为树形结构
     *
     * @param voList 平铺的菜单VO列表
     * @return 树形菜单VO列表
     */
    private List<MenuTreeVO> buildTree(List<MenuTreeVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }
        // 按 parentId 分组
        Map<Long, List<MenuTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(MenuTreeVO::getParentId));
        // 为每个节点设置 children
        voList.forEach(vo -> vo.setChildren(parentMap.getOrDefault(vo.getId(), new ArrayList<>())));
        // 返回根节点列表
        return voList.stream()
                .filter(vo -> ROOT_PARENT_ID == vo.getParentId())
                .collect(Collectors.toList());
    }
}
