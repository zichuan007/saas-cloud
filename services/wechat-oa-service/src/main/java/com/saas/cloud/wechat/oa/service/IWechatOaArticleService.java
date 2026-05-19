package com.saas.cloud.wechat.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.entity.WechatOaArticle;

/**
 * 图文表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaArticleService extends IService<WechatOaArticle> {

    PageResult<WechatOaArticle> pageArticles(Long accountId, Byte status, Integer pageNum, Integer pageSize);

    void createArticle(WechatOaArticle article);

    void updateArticle(Long id, WechatOaArticle article);

    void deleteArticle(Long id);

    void publishArticle(Long id);

    void offlineArticle(Long id);

    void previewArticle(Long id, String openid);
}
