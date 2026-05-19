package com.saas.cloud.wechat.oa.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaArticle;
import com.saas.cloud.wechat.oa.mapper.WechatOaArticleMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 图文表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaArticleServiceImpl
        extends ServiceImpl<WechatOaArticleMapper, WechatOaArticle>
        implements IWechatOaArticleService {

    private static final byte STATUS_DRAFT = 0;
    private static final byte STATUS_PUBLISHED = 1;
    private static final byte STATUS_OFFLINE = 2;

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Override
    public PageResult<WechatOaArticle> pageArticles(Long accountId, Byte status,
                                                     Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WechatOaArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatOaArticle::getAccountId, accountId)
                .eq(status != null, WechatOaArticle::getStatus, status)
                .orderByDesc(WechatOaArticle::getCreateTime);

        Page<WechatOaArticle> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void createArticle(WechatOaArticle article) {
        article.setStatus(STATUS_DRAFT);
        article.setReadCount(0);
        article.setShareCount(0);
        article.setLikeCount(0);
        save(article);
        log.info("创建图文成功, id={}, title={}", article.getId(), article.getTitle());
    }

    @Override
    public void updateArticle(Long id, WechatOaArticle article) {
        WechatOaArticle existing = getById(id);
        if (existing == null) {
            throw new BusinessException("图文不存在");
        }
        if (existing.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException("已发布的图文不能编辑，请先下线");
        }

        existing.setTitle(article.getTitle());
        existing.setAuthor(article.getAuthor());
        existing.setDigest(article.getDigest());
        existing.setContent(article.getContent());
        existing.setThumbMediaId(article.getThumbMediaId());
        existing.setThumbUrl(article.getThumbUrl());
        existing.setContentSourceUrl(article.getContentSourceUrl());
        existing.setSortOrder(article.getSortOrder());
        updateById(existing);
        log.info("更新图文成功, id={}", id);
    }

    @Override
    public void deleteArticle(Long id) {
        WechatOaArticle existing = getById(id);
        if (existing == null) {
            throw new BusinessException("图文不存在");
        }
        if (existing.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException("已发布的图文不能删除，请先下线");
        }
        removeById(id);
        log.info("删除图文成功, id={}", id);
    }

    @Override
    public void publishArticle(Long id) {
        WechatOaArticle existing = getById(id);
        if (existing == null) {
            throw new BusinessException("图文不存在");
        }
        if (existing.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException("图文已处于发布状态");
        }

        existing.setStatus(STATUS_PUBLISHED);
        existing.setPublishTime(LocalDateTime.now());
        updateById(existing);
        log.info("发布图文成功, id={}, title={}", id, existing.getTitle());
    }

    @Override
    public void offlineArticle(Long id) {
        WechatOaArticle existing = getById(id);
        if (existing == null) {
            throw new BusinessException("图文不存在");
        }
        if (existing.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException("只有已发布的图文才能下线");
        }

        existing.setStatus(STATUS_OFFLINE);
        updateById(existing);
        log.info("下线图文成功, id={}", id);
    }

    @Override
    public void previewArticle(Long id, String openid) {
        WechatOaArticle article = getById(id);
        if (article == null) {
            throw new BusinessException("图文不存在");
        }
        if (StrUtil.isBlank(article.getWxMediaId())) {
            throw new BusinessException("图文尚未同步到微信，无法预览");
        }

        String accessToken = accountService.getValidAccessToken(article.getAccountId());
        wechatApiClient.previewMessage(accessToken, openid, article.getWxMediaId());
        log.info("预览图文成功, id={}, openid={}", id, openid);
    }
}
