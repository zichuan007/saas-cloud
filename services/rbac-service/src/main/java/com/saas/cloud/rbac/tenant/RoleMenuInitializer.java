package com.saas.cloud.rbac.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializer;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.entity.Menu;
import com.saas.cloud.rbac.entity.RoleMenu;
import com.saas.cloud.rbac.mapper.MenuMapper;
import com.saas.cloud.rbac.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户初始化 - 为超管角色分配套餐菜单权限
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RoleMenuInitializer implements TenantInitializer {

    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final PlatformFeignClient platformFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public String getCode() {
        return "ROLE_MENU";
    }

    @Override
    public int getOrder() {
        return 35;
    }

    @Override
    public void initialize(TenantInitContext context) {
        Long adminRoleId = context.get("adminRoleId");
        List<Long> menuIds = resolveMenuIds(context.getTenantId());

        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(adminRoleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setTenantId(context.getTenantId());
            roleMenuMapper.insert(roleMenu);
        }

        context.put("roleMenuCount", menuIds.size());
        log.info("初始化超管菜单权限成功, roleId={}, menuCount={}", adminRoleId, menuIds.size());
    }

    @Override
    public void rollback(TenantInitContext context) {
        Long adminRoleId = context.get("adminRoleId");
        if (adminRoleId != null) {
            roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                    .eq(RoleMenu::getRoleId, adminRoleId));
            log.info("回滚超管菜单权限, roleId={}", adminRoleId);
        }
    }

    /**
     * 解析套餐可见菜单ID；如果套餐未配置 menuIds 则返回全部启用菜单
     */
    private List<Long> resolveMenuIds(Long tenantId) {
        try {
            ApiResult<TenantVO> result = TenantContext.executeWithoutTenant(
                    () -> platformFeignClient.getTenantInfo(tenantId));
            if (result != null && result.getData() != null
                    && StringUtils.hasText(result.getData().getMenuIds())) {
                return objectMapper.readValue(
                        result.getData().getMenuIds(), new TypeReference<List<Long>>() {});
            }
        } catch (Exception e) {
            log.warn("获取套餐菜单失败, 降级使用全部菜单, tenantId={}, error={}", tenantId, e.getMessage());
        }
        // 旗舰版或未配置：返回全部启用菜单
        return TenantContext.executeWithoutTenant(() ->
                menuMapper.selectList(new LambdaQueryWrapper<Menu>().eq(Menu::getStatus, (byte) 1))
                        .stream()
                        .map(Menu::getId)
                        .collect(Collectors.toList()));
    }
}
