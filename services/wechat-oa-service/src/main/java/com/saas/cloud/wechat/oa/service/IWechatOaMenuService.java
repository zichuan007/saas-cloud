package com.saas.cloud.wechat.oa.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.wechat.oa.entity.WechatOaMenu;

/**
 * 公众号菜单表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaMenuService extends IService<WechatOaMenu> {

    List<WechatOaMenu> listMenus(Long accountId);

    void saveMenus(Long accountId, List<WechatOaMenu> menus);

    /**
     * 从前端树结构保存菜单，自动处理父子关系
     *
     * @param accountId 公众号ID
     * @param buttons   前端菜单树
     */
    void saveMenusFromTree(Long accountId, List<Map<String, Object>> buttons);

    void publishMenus(Long accountId);
}
