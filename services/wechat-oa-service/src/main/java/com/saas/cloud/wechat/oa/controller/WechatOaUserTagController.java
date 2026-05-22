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
import com.saas.cloud.wechat.oa.entity.WechatOaUserTag;
import com.saas.cloud.wechat.oa.service.IWechatOaUserTagService;

import lombok.RequiredArgsConstructor;

/**
 * 粉丝标签控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaUserTagController {

    private final IWechatOaUserTagService userTagService;

    /**
     * 标签列表
     *
     * @param accountId 公众号ID
     * @return 标签列表
     */
    @GetMapping("/list")
    public ApiResult<List<WechatOaUserTag>> list(@RequestParam(required = false) Long accountId) {
        return ApiResult.ok(userTagService.listTags(accountId));
    }

    /**
     * 创建标签
     *
     * @param tag 标签信息
     * @return 操作结果
     */
    @OperationLog(module = "粉丝标签", operation = "创建标签")
    @PostMapping
    public ApiResult<Void> create(@RequestBody WechatOaUserTag tag) {
        userTagService.createTag(tag);
        return ApiResult.ok();
    }

    /**
     * 编辑标签
     *
     * @param id  标签ID
     * @param tag 标签信息
     * @return 操作结果
     */
    @OperationLog(module = "粉丝标签", operation = "编辑标签")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody WechatOaUserTag tag) {
        userTagService.updateTag(id, tag);
        return ApiResult.ok();
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 操作结果
     */
    @OperationLog(module = "粉丝标签", operation = "删除标签")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        userTagService.deleteTag(id);
        return ApiResult.ok();
    }

    /**
     * 同步标签到微信
     *
     * @param accountId 公众号ID
     * @return 操作结果
     */
    @OperationLog(module = "粉丝标签", operation = "同步标签")
    @PostMapping("/sync")
    public ApiResult<Void> syncTags(@RequestParam Long accountId) {
        userTagService.syncTags(accountId);
        return ApiResult.ok();
    }
}
