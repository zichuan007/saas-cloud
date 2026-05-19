package com.saas.cloud.wechat.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.wechat.oa.entity.WechatOaArticle;
import com.saas.cloud.wechat.oa.entity.WechatOaFanUser;
import com.saas.cloud.wechat.oa.service.IWechatOaArticleService;
import com.saas.cloud.wechat.oa.service.IWechatOaFanUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公众号数据看板控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaDashboardController {

    private final IWechatOaFanUserService fanUserService;
    private final IWechatOaArticleService articleService;

    /**
     * 粉丝趋势（新增/取关/净增）
     *
     * @param accountId 公众号ID
     * @param days      统计天数（默认7天）
     * @return 趋势数据
     */
    @GetMapping("/fan-trend")
    public ApiResult<List<Map<String, Object>>> fanTrend(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "7") Integer days) {

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            long newFans = fanUserService.lambdaQuery()
                    .eq(WechatOaFanUser::getAccountId, accountId)
                    .ge(WechatOaFanUser::getSubscribeTime, dayStart)
                    .le(WechatOaFanUser::getSubscribeTime, dayEnd)
                    .count();

            long unfollowed = fanUserService.lambdaQuery()
                    .eq(WechatOaFanUser::getAccountId, accountId)
                    .ge(WechatOaFanUser::getUnsubscribeTime, dayStart)
                    .le(WechatOaFanUser::getUnsubscribeTime, dayEnd)
                    .count();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("newFans", newFans);
            dayData.put("unfollowed", unfollowed);
            dayData.put("netGrowth", newFans - unfollowed);
            result.add(dayData);
        }
        return ApiResult.ok(result);
    }

    /**
     * 图文排行（按阅读量排序）
     *
     * @param accountId 公众号ID
     * @param limit     返回条数（默认10）
     * @return 图文排行数据
     */
    @GetMapping("/article-rank")
    public ApiResult<List<Map<String, Object>>> articleRank(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "10") Integer limit) {

        LambdaQueryWrapper<WechatOaArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatOaArticle::getAccountId, accountId)
                .eq(WechatOaArticle::getStatus, (byte) 1)
                .orderByDesc(WechatOaArticle::getReadCount)
                .last("LIMIT " + limit);

        List<WechatOaArticle> articles = articleService.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (WechatOaArticle article : articles) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("readCount", article.getReadCount());
            item.put("shareCount", article.getShareCount());
            item.put("likeCount", article.getLikeCount());
            item.put("publishTime", article.getPublishTime());
            result.add(item);
        }
        return ApiResult.ok(result);
    }
}
