package com.saas.cloud.wechat.oa.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaFanUser;
import com.saas.cloud.wechat.oa.mapper.WechatOaFanUserMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaFanUserService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 粉丝表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaFanUserServiceImpl
        extends ServiceImpl<WechatOaFanUserMapper, WechatOaFanUser>
        implements IWechatOaFanUserService {

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Override
    public PageResult<WechatOaFanUser> pageFans(Long accountId, String nickname,
                                                 Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WechatOaFanUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatOaFanUser::getAccountId, accountId)
                .like(StrUtil.isNotBlank(nickname), WechatOaFanUser::getNickname, nickname)
                .orderByDesc(WechatOaFanUser::getSubscribeTime);

        Page<WechatOaFanUser> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void updateBlacklist(Long id, boolean blacklisted) {
        WechatOaFanUser fan = getById(id);
        if (fan == null) {
            throw new BusinessException("粉丝不存在");
        }

        LambdaUpdateWrapper<WechatOaFanUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WechatOaFanUser::getId, id)
                .set(WechatOaFanUser::getIsBlacklisted, blacklisted ? (byte) 1 : (byte) 0);
        update(wrapper);
        log.info("更新粉丝黑名单状态, id={}, blacklisted={}", id, blacklisted);
    }

    @Override
    public void updateTags(Long id, String tagIds) {
        WechatOaFanUser fan = getById(id);
        if (fan == null) {
            throw new BusinessException("粉丝不存在");
        }

        LambdaUpdateWrapper<WechatOaFanUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WechatOaFanUser::getId, id)
                .set(WechatOaFanUser::getTagIds, tagIds);
        update(wrapper);
        log.info("更新粉丝标签, id={}", id);
    }

    @Override
    public WechatOaFanUser getByOpenid(Long accountId, String openid) {
        return lambdaQuery()
                .eq(WechatOaFanUser::getAccountId, accountId)
                .eq(WechatOaFanUser::getOpenid, openid)
                .one();
    }

    @Override
    public void saveOrUpdateFan(WechatOaFanUser fanUser) {
        WechatOaFanUser existing = getByOpenid(fanUser.getAccountId(), fanUser.getOpenid());
        if (existing != null) {
            fanUser.setId(existing.getId());
            updateById(fanUser);
        } else {
            save(fanUser);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void syncFans(Long accountId) {
        String accessToken = accountService.getValidAccessToken(accountId);
        String nextOpenid = "";
        int totalSynced = 0;

        do {
            Map<String, Object> result = wechatApiClient.getUserList(accessToken, nextOpenid);
            Integer total = (Integer) result.get("total");
            Integer count = (Integer) result.get("count");
            nextOpenid = (String) result.get("next_openid");

            if (count == null || count == 0) {
                break;
            }

            Map<String, Object> data = (Map<String, Object>) result.get("data");
            List<String> openids = (List<String>) data.get("openid");

            for (int i = 0; i < openids.size(); i += 100) {
                int end = Math.min(i + 100, openids.size());
                List<String> batch = openids.subList(i, end);
                List<Map<String, Object>> userInfos = wechatApiClient.batchGetUserInfo(
                        accessToken, batch);
                for (Map<String, Object> info : userInfos) {
                    saveFanFromWechat(accountId, info);
                }
                totalSynced += userInfos.size();
            }

            log.info("同步粉丝进度, accountId={}, synced={}, total={}", accountId, totalSynced, total);
        } while (StrUtil.isNotBlank(nextOpenid));

        log.info("全量同步粉丝完成, accountId={}, totalSynced={}", accountId, totalSynced);
    }

    private void saveFanFromWechat(Long accountId, Map<String, Object> info) {
        String openid = (String) info.get("openid");
        WechatOaFanUser fan = getByOpenid(accountId, openid);
        if (fan == null) {
            fan = new WechatOaFanUser();
            fan.setAccountId(accountId);
            fan.setOpenid(openid);
        }
        fan.setUnionid((String) info.get("unionid"));
        fan.setNickname((String) info.get("nickname"));
        fan.setAvatarUrl((String) info.get("headimgurl"));
        Object gender = info.get("sex");
        if (gender instanceof Integer) {
            fan.setGender(((Integer) gender).byteValue());
        }
        fan.setCountry((String) info.get("country"));
        fan.setProvince((String) info.get("province"));
        fan.setCity((String) info.get("city"));
        fan.setLanguage((String) info.get("language"));
        Object subscribe = info.get("subscribe");
        fan.setSubscribeStatus(subscribe != null && subscribe.equals(1) ? (byte) 1 : (byte) 0);
        Object subscribeTime = info.get("subscribe_time");
        if (subscribeTime instanceof Integer) {
            fan.setSubscribeTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond((Integer) subscribeTime), ZoneId.systemDefault()));
        }
        fan.setSubscribeScene((String) info.get("subscribe_scene"));
        saveOrUpdateFan(fan);
    }
}
