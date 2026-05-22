package com.saas.cloud.wechat.oa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.wechat.oa.entity.WechatOaAutoReplyRule;
import com.saas.cloud.wechat.oa.service.IWechatOaAutoReplyRuleService;

import lombok.RequiredArgsConstructor;

/**
 * 自动回复规则控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/auto-reply")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaAutoReplyRuleController {

    private final IWechatOaAutoReplyRuleService autoReplyRuleService;

    /**
     * 自动回复规则列表
     *
     * @param accountId 公众号ID
     * @return 规则列表
     */
    @GetMapping("/list")
    public ApiResult<List<WechatOaAutoReplyRule>> list(@RequestParam(required = false) Long accountId) {
        return ApiResult.ok(autoReplyRuleService.listRules(accountId));
    }

    /**
     * 创建规则
     *
     * @param rule 规则信息
     * @return 操作结果
     */
    @OperationLog(module = "自动回复", operation = "创建规则")
    @PostMapping
    public ApiResult<Void> create(@RequestBody WechatOaAutoReplyRule rule) {
        autoReplyRuleService.createRule(rule);
        return ApiResult.ok();
    }

    /**
     * 编辑规则
     *
     * @param id   规则ID
     * @param rule 规则信息
     * @return 操作结果
     */
    @OperationLog(module = "自动回复", operation = "编辑规则")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody WechatOaAutoReplyRule rule) {
        autoReplyRuleService.updateRule(id, rule);
        return ApiResult.ok();
    }

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 操作结果
     */
    @OperationLog(module = "自动回复", operation = "删除规则")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        autoReplyRuleService.deleteRule(id);
        return ApiResult.ok();
    }

    /**
     * 启用/禁用规则
     *
     * @param id     规则ID
     * @param status 状态（0-禁用 1-启用）
     * @return 操作结果
     */
    @OperationLog(module = "自动回复", operation = "更新规则状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @RequestParam Byte status) {
        autoReplyRuleService.updateStatus(id, status);
        return ApiResult.ok();
    }
}
