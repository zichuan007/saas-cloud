package com.saas.cloud.wechat.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.wechat.oa.entity.WechatOaAutoReplyRule;

import java.util.List;

/**
 * 自动回复规则表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaAutoReplyRuleService extends IService<WechatOaAutoReplyRule> {

    List<WechatOaAutoReplyRule> listRules(Long accountId);

    void createRule(WechatOaAutoReplyRule rule);

    void updateRule(Long id, WechatOaAutoReplyRule rule);

    void deleteRule(Long id);

    void updateStatus(Long id, Byte status);

    WechatOaAutoReplyRule matchRule(Long accountId, Byte ruleType, String keyword);
}
