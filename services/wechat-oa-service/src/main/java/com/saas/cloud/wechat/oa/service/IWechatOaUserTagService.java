package com.saas.cloud.wechat.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.wechat.oa.entity.WechatOaUserTag;

import java.util.List;

/**
 * 粉丝标签表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaUserTagService extends IService<WechatOaUserTag> {

    List<WechatOaUserTag> listTags(Long accountId);

    void createTag(WechatOaUserTag tag);

    void updateTag(Long id, WechatOaUserTag tag);

    void deleteTag(Long id);

    void syncTags(Long accountId);
}
