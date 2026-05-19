package com.saas.cloud.wechat.oa.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.wechat.oa.entity.WechatOaMenu;
import com.saas.cloud.wechat.oa.service.IWechatOaMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 公众号菜单控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaMenuController {

    private final IWechatOaMenuService menuService;

    /**
     * 当前菜单配置
     *
     * @param accountId 公众号ID
     * @return 菜单列表
     */
    @GetMapping("/list")
    public ApiResult<List<WechatOaMenu>> list(@RequestParam Long accountId) {
        return ApiResult.ok(menuService.listMenus(accountId));
    }

    /**
     * 保存菜单配置
     *
     * @param body 包含 accountId 和 buttons 的 JSON
     * @return 操作结果
     */
    @OperationLog(module = "菜单管理", operation = "保存菜单配置")
    @PostMapping("/save")
    @SuppressWarnings("unchecked")
    public ApiResult<Void> save(@RequestBody Map<String, Object> body) {
        Long accountId = Long.valueOf(String.valueOf(body.get("accountId")));
        List<Map<String, Object>> buttons = (List<Map<String, Object>>) body.getOrDefault("buttons", Collections.emptyList());
        menuService.saveMenusFromTree(accountId, buttons);
        return ApiResult.ok();
    }

    /**
     * 发布菜单到微信
     *
     * @param body 包含 accountId 的 JSON
     * @return 操作结果
     */
    @OperationLog(module = "菜单管理", operation = "发布菜单到微信")
    @PostMapping("/publish")
    public ApiResult<Void> publish(@RequestBody Map<String, Object> body) {
        Long accountId = Long.valueOf(String.valueOf(body.get("accountId")));
        menuService.publishMenus(accountId);
        return ApiResult.ok();
    }
}
