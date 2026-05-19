package com.saas.cloud.wechat.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.entity.WechatOaFanUser;

/**
 * 粉丝表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaFanUserService extends IService<WechatOaFanUser> {

    PageResult<WechatOaFanUser> pageFans(Long accountId, String nickname,
                                         Integer pageNum, Integer pageSize);

    void updateBlacklist(Long id, boolean blacklisted);

    void updateTags(Long id, String tagIds);

    WechatOaFanUser getByOpenid(Long accountId, String openid);

    void saveOrUpdateFan(WechatOaFanUser fanUser);

    void syncFans(Long accountId);
}
