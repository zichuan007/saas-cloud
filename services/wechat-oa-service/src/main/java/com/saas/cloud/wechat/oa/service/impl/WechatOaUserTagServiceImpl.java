package com.saas.cloud.wechat.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaUserTag;
import com.saas.cloud.wechat.oa.mapper.WechatOaUserTagMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaUserTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 粉丝标签表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaUserTagServiceImpl
        extends ServiceImpl<WechatOaUserTagMapper, WechatOaUserTag>
        implements IWechatOaUserTagService {

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Override
    public List<WechatOaUserTag> listTags(Long accountId) {
        LambdaQueryWrapper<WechatOaUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(accountId != null, WechatOaUserTag::getAccountId, accountId)
                .orderByAsc(WechatOaUserTag::getTagName);
        return list(wrapper);
    }

    @Override
    public void createTag(WechatOaUserTag tag) {
        long count = lambdaQuery()
                .eq(WechatOaUserTag::getAccountId, tag.getAccountId())
                .eq(WechatOaUserTag::getTagName, tag.getTagName())
                .count();
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setFanCount(0);
        save(tag);
        log.info("创建粉丝标签, id={}, tagName={}", tag.getId(), tag.getTagName());
    }

    @Override
    public void updateTag(Long id, WechatOaUserTag tag) {
        WechatOaUserTag existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标签不存在");
        }

        long count = lambdaQuery()
                .eq(WechatOaUserTag::getAccountId, existing.getAccountId())
                .eq(WechatOaUserTag::getTagName, tag.getTagName())
                .ne(WechatOaUserTag::getId, id)
                .count();
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        existing.setTagName(tag.getTagName());
        updateById(existing);
        log.info("更新粉丝标签, id={}", id);
    }

    @Override
    public void deleteTag(Long id) {
        WechatOaUserTag existing = getById(id);
        if (existing == null) {
            throw new BusinessException("标签不存在");
        }
        if (existing.getFanCount() != null && existing.getFanCount() > 0) {
            throw new BusinessException("该标签下还有粉丝，无法删除");
        }
        removeById(id);
        log.info("删除粉丝标签, id={}, tagName={}", id, existing.getTagName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void syncTags(Long accountId) {
        String accessToken = accountService.getValidAccessToken(accountId);
        Map<String, Object> result = wechatApiClient.getTags(accessToken);
        List<Map<String, Object>> wxTags = (List<Map<String, Object>>) result.get("tags");

        if (wxTags == null) {
            log.warn("微信返回标签列表为空, accountId={}", accountId);
            return;
        }

        int synced = 0;
        for (Map<String, Object> wxTag : wxTags) {
            Integer wxTagId = (Integer) wxTag.get("id");
            String tagName = (String) wxTag.get("name");
            Integer count = (Integer) wxTag.get("count");

            WechatOaUserTag existing = lambdaQuery()
                    .eq(WechatOaUserTag::getAccountId, accountId)
                    .eq(WechatOaUserTag::getWxTagId, wxTagId)
                    .one();

            if (existing == null) {
                WechatOaUserTag tag = new WechatOaUserTag();
                tag.setAccountId(accountId);
                tag.setWxTagId(wxTagId);
                tag.setTagName(tagName);
                tag.setFanCount(count != null ? count : 0);
                save(tag);
            } else {
                existing.setTagName(tagName);
                existing.setFanCount(count != null ? count : 0);
                updateById(existing);
            }
            synced++;
        }
        log.info("同步标签完成, accountId={}, synced={}", accountId, synced);
    }
}
