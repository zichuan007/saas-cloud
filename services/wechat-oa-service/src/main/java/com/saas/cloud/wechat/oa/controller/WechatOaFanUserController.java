package com.saas.cloud.wechat.oa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.wechat.oa.entity.WechatOaFanUser;
import com.saas.cloud.wechat.oa.service.IWechatOaFanUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 粉丝管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "公众号粉丝管理")
@RestController
@RequestMapping("/fan")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaFanUserController {

    private final IWechatOaFanUserService fanUserService;

    /**
     * 粉丝列表
     *
     * @param accountId 公众号ID
     * @param nickname  昵称（模糊查询）
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    @Operation(summary = "粉丝列表")
    @GetMapping("/list")
    public ApiResult<PageResult<WechatOaFanUser>> list(@RequestParam Long accountId,
                                                        @RequestParam(required = false) String nickname,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResult.ok(fanUserService.pageFans(accountId, nickname, pageNum, pageSize));
    }

    /**
     * 拉黑/取消拉黑
     *
     * @param id          粉丝ID
     * @param blacklisted 是否拉黑
     * @return 操作结果
     */
    /**
     * 全量同步粉丝
     *
     * @param accountId 公众号ID
     * @return 操作结果
     */
    @Operation(summary = "全量同步粉丝")
    @OperationLog(module = "粉丝管理", operation = "全量同步粉丝")
    @PostMapping("/sync")
    public ApiResult<Void> syncFans(@RequestParam Long accountId) {
        fanUserService.syncFans(accountId);
        return ApiResult.ok();
    }

    @Operation(summary = "拉黑/取消拉黑")
    @OperationLog(module = "粉丝管理", operation = "更新黑名单状态")
    @PutMapping("/{id}/blacklist")
    public ApiResult<Void> updateBlacklist(@PathVariable("id") Long id,
                                           @RequestParam boolean blacklisted) {
        fanUserService.updateBlacklist(id, blacklisted);
        return ApiResult.ok();
    }

    /**
     * 设置粉丝标签
     *
     * @param id     粉丝ID
     * @param tagIds 标签ID列表（JSON格式）
     * @return 操作结果
     */
    @Operation(summary = "设置粉丝标签")
    @OperationLog(module = "粉丝管理", operation = "设置粉丝标签")
    @PutMapping("/{id}/tags")
    public ApiResult<Void> updateTags(@PathVariable("id") Long id,
                                      @RequestParam String tagIds) {
        fanUserService.updateTags(id, tagIds);
        return ApiResult.ok();
    }
}
