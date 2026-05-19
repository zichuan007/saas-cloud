package com.saas.cloud.wechat.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaMenu;
import com.saas.cloud.wechat.oa.mapper.WechatOaMenuMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公众号菜单表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaMenuServiceImpl
        extends ServiceImpl<WechatOaMenuMapper, WechatOaMenu>
        implements IWechatOaMenuService {

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Override
    public List<WechatOaMenu> listMenus(Long accountId) {
        LambdaQueryWrapper<WechatOaMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatOaMenu::getAccountId, accountId)
                .orderByAsc(WechatOaMenu::getParentId)
                .orderByAsc(WechatOaMenu::getSortOrder);
        return list(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveMenus(Long accountId, List<WechatOaMenu> menus) {
        lambdaUpdate()
                .eq(WechatOaMenu::getAccountId, accountId)
                .remove();

        if (menus != null && !menus.isEmpty()) {
            for (WechatOaMenu menu : menus) {
                menu.setId(null);
                menu.setAccountId(accountId);
            }
            saveBatch(menus);
        }
        log.info("保存公众号菜单, accountId={}, menuCount={}", accountId,
                menus != null ? menus.size() : 0);
    }

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveMenusFromTree(Long accountId, List<Map<String, Object>> buttons) {
        lambdaUpdate()
                .eq(WechatOaMenu::getAccountId, accountId)
                .remove();

        if (buttons == null || buttons.isEmpty()) {
            log.info("清空公众号菜单, accountId={}", accountId);
            return;
        }

        int sortOrder = 0;
        for (Map<String, Object> btn : buttons) {
            WechatOaMenu root = new WechatOaMenu();
            root.setAccountId(accountId);
            root.setMenuName((String) btn.get("name"));
            root.setMenuType((String) btn.get("type"));
            root.setMenuKey((String) btn.get("key"));
            root.setMenuUrl((String) btn.get("url"));
            root.setParentId(0L);
            root.setSortOrder(sortOrder++);
            save(root);

            List<Map<String, Object>> subButtons = (List<Map<String, Object>>) btn.getOrDefault("subButtons", Collections.emptyList());
            if (subButtons != null && !subButtons.isEmpty()) {
                List<WechatOaMenu> children = new ArrayList<>();
                for (Map<String, Object> sub : subButtons) {
                    WechatOaMenu child = new WechatOaMenu();
                    child.setAccountId(accountId);
                    child.setMenuName((String) sub.get("name"));
                    child.setMenuType((String) sub.get("type"));
                    child.setMenuKey((String) sub.get("key"));
                    child.setMenuUrl((String) sub.get("url"));
                    child.setParentId(root.getId());
                    child.setSortOrder(sortOrder++);
                    children.add(child);
                }
                saveBatch(children);
            }
        }
        log.info("从树结构保存公众号菜单, accountId={}, rootCount={}", accountId, buttons.size());
    }

    @Override
    public void publishMenus(Long accountId) {
        List<WechatOaMenu> menus = listMenus(accountId);
        String accessToken = accountService.getValidAccessToken(accountId);

        List<WechatOaMenu> topMenus = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());

        List<Map<String, Object>> buttons = new ArrayList<>();
        for (WechatOaMenu topMenu : topMenus) {
            List<WechatOaMenu> subMenus = menus.stream()
                    .filter(m -> topMenu.getId().equals(m.getParentId()))
                    .collect(Collectors.toList());

            Map<String, Object> button = new HashMap<>();
            button.put("name", topMenu.getMenuName());

            if (subMenus.isEmpty()) {
                button.put("type", topMenu.getMenuType());
                fillButtonAction(button, topMenu);
            } else {
                List<Map<String, Object>> subButtons = new ArrayList<>();
                for (WechatOaMenu sub : subMenus) {
                    Map<String, Object> subBtn = new HashMap<>();
                    subBtn.put("name", sub.getMenuName());
                    subBtn.put("type", sub.getMenuType());
                    fillButtonAction(subBtn, sub);
                    subButtons.add(subBtn);
                }
                button.put("sub_button", subButtons);
            }
            buttons.add(button);
        }

        Map<String, Object> menuData = new HashMap<>();
        menuData.put("button", buttons);
        wechatApiClient.createMenu(accessToken, menuData);
        log.info("发布菜单到微信成功, accountId={}", accountId);
    }

    private void fillButtonAction(Map<String, Object> button, WechatOaMenu menu) {
        if ("view".equals(menu.getMenuType())) {
            button.put("url", menu.getMenuUrl());
        } else if ("click".equals(menu.getMenuType())) {
            button.put("key", menu.getMenuKey());
        } else if ("miniprogram".equals(menu.getMenuType())) {
            button.put("url", menu.getMenuUrl());
            button.put("appid", menu.getMenuKey());
        } else if ("media_id".equals(menu.getMenuType())
                || "view_limited".equals(menu.getMenuType())) {
            button.put("media_id", menu.getMediaId());
        } else {
            button.put("key", menu.getMenuKey());
        }
    }
}
