package com.saas.cloud.wechat.oa.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.wechat.oa.entity.WechatOaAutoReplyRule;
import com.saas.cloud.wechat.oa.mapper.WechatOaAutoReplyRuleMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAutoReplyRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自动回复规则表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class WechatOaAutoReplyRuleServiceImpl
        extends ServiceImpl<WechatOaAutoReplyRuleMapper, WechatOaAutoReplyRule>
        implements IWechatOaAutoReplyRuleService {

    private static final byte RULE_TYPE_FOLLOW = 0;
    private static final byte RULE_TYPE_KEYWORD = 1;
    private static final byte RULE_TYPE_DEFAULT = 2;

    private static final byte MATCH_TYPE_EXACT = 0;
    private static final byte MATCH_TYPE_PARTIAL = 1;

    @Override
    public List<WechatOaAutoReplyRule> listRules(Long accountId) {
        LambdaQueryWrapper<WechatOaAutoReplyRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(accountId != null, WechatOaAutoReplyRule::getAccountId, accountId)
                .orderByAsc(WechatOaAutoReplyRule::getRuleType)
                .orderByAsc(WechatOaAutoReplyRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    public void createRule(WechatOaAutoReplyRule rule) {
        if (rule.getRuleType() == RULE_TYPE_FOLLOW || rule.getRuleType() == RULE_TYPE_DEFAULT) {
            long count = lambdaQuery()
                    .eq(WechatOaAutoReplyRule::getAccountId, rule.getAccountId())
                    .eq(WechatOaAutoReplyRule::getRuleType, rule.getRuleType())
                    .count();
            if (count > 0) {
                throw new BusinessException("该类型的回复规则已存在，请直接编辑");
            }
        }

        rule.setStatus((byte) 1);
        save(rule);
        log.info("创建自动回复规则, id={}, ruleName={}", rule.getId(), rule.getRuleName());
    }

    @Override
    public void updateRule(Long id, WechatOaAutoReplyRule rule) {
        WechatOaAutoReplyRule existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        existing.setRuleName(rule.getRuleName());
        existing.setKeyword(rule.getKeyword());
        existing.setMatchType(rule.getMatchType());
        existing.setReplyType(rule.getReplyType());
        existing.setReplyContent(rule.getReplyContent());
        existing.setReplyMediaId(rule.getReplyMediaId());
        existing.setSortOrder(rule.getSortOrder());
        updateById(existing);
        log.info("更新自动回复规则, id={}", id);
    }

    @Override
    public void deleteRule(Long id) {
        WechatOaAutoReplyRule existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }
        removeById(id);
        log.info("删除自动回复规则, id={}, ruleName={}", id, existing.getRuleName());
    }

    @Override
    public void updateStatus(Long id, Byte status) {
        WechatOaAutoReplyRule existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        existing.setStatus(status);
        updateById(existing);
        log.info("更新规则状态, id={}, status={}", id, status);
    }

    @Override
    public WechatOaAutoReplyRule matchRule(Long accountId, Byte ruleType, String keyword) {
        if (ruleType == RULE_TYPE_KEYWORD && StrUtil.isNotBlank(keyword)) {
            WechatOaAutoReplyRule exactMatch = lambdaQuery()
                    .eq(WechatOaAutoReplyRule::getAccountId, accountId)
                    .eq(WechatOaAutoReplyRule::getRuleType, RULE_TYPE_KEYWORD)
                    .eq(WechatOaAutoReplyRule::getMatchType, MATCH_TYPE_EXACT)
                    .eq(WechatOaAutoReplyRule::getKeyword, keyword)
                    .eq(WechatOaAutoReplyRule::getStatus, (byte) 1)
                    .orderByAsc(WechatOaAutoReplyRule::getSortOrder)
                    .last("LIMIT 1")
                    .one();
            if (exactMatch != null) {
                return exactMatch;
            }

            WechatOaAutoReplyRule partialMatch = lambdaQuery()
                    .eq(WechatOaAutoReplyRule::getAccountId, accountId)
                    .eq(WechatOaAutoReplyRule::getRuleType, RULE_TYPE_KEYWORD)
                    .eq(WechatOaAutoReplyRule::getMatchType, MATCH_TYPE_PARTIAL)
                    .like(WechatOaAutoReplyRule::getKeyword, keyword)
                    .eq(WechatOaAutoReplyRule::getStatus, (byte) 1)
                    .orderByAsc(WechatOaAutoReplyRule::getSortOrder)
                    .last("LIMIT 1")
                    .one();
            if (partialMatch != null) {
                return partialMatch;
            }
        }

        return lambdaQuery()
                .eq(WechatOaAutoReplyRule::getAccountId, accountId)
                .eq(WechatOaAutoReplyRule::getRuleType, ruleType)
                .eq(WechatOaAutoReplyRule::getStatus, (byte) 1)
                .orderByAsc(WechatOaAutoReplyRule::getSortOrder)
                .last("LIMIT 1")
                .one();
    }
}
