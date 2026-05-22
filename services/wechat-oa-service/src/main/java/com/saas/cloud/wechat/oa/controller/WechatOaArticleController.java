package com.saas.cloud.wechat.oa.controller;

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
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.wechat.oa.entity.WechatOaArticle;
import com.saas.cloud.wechat.oa.service.IWechatOaArticleService;

import lombok.RequiredArgsConstructor;

/**
 * 图文管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaArticleController {

    private final IWechatOaArticleService articleService;

    /**
     * 图文列表
     *
     * @param accountId 公众号ID
     * @param status    状态
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResult<PageResult<WechatOaArticle>> list(@RequestParam Long accountId,
                                                        @RequestParam(required = false) Byte status,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResult.ok(articleService.pageArticles(accountId, status, pageNum, pageSize));
    }

    /**
     * 图文详情
     *
     * @param id 图文ID
     * @return 图文详情
     */
    @GetMapping("/{id}")
    public ApiResult<WechatOaArticle> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(articleService.getById(id));
    }

    /**
     * 创建图文
     *
     * @param article 图文信息
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "创建图文")
    @PostMapping
    public ApiResult<Void> create(@RequestBody WechatOaArticle article) {
        articleService.createArticle(article);
        return ApiResult.ok();
    }

    /**
     * 编辑图文
     *
     * @param id      图文ID
     * @param article 图文信息
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "编辑图文")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody WechatOaArticle article) {
        articleService.updateArticle(id, article);
        return ApiResult.ok();
    }

    /**
     * 删除图文
     *
     * @param id 图文ID
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "删除图文")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        articleService.deleteArticle(id);
        return ApiResult.ok();
    }

    /**
     * 预览图文
     *
     * @param id     图文ID
     * @param openid 预览接收者的openid
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "预览图文")
    @PostMapping("/{id}/preview")
    public ApiResult<Void> preview(@PathVariable("id") Long id,
                                   @RequestParam String openid) {
        articleService.previewArticle(id, openid);
        return ApiResult.ok();
    }

    /**
     * 发布图文
     *
     * @param id 图文ID
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "发布图文")
    @PostMapping("/{id}/publish")
    public ApiResult<Void> publish(@PathVariable("id") Long id) {
        articleService.publishArticle(id);
        return ApiResult.ok();
    }

    /**
     * 下线图文
     *
     * @param id 图文ID
     * @return 操作结果
     */
    @OperationLog(module = "图文管理", operation = "下线图文")
    @PutMapping("/{id}/offline")
    public ApiResult<Void> offline(@PathVariable("id") Long id) {
        articleService.offlineArticle(id);
        return ApiResult.ok();
    }
}
