package com.saas.cloud.wechat.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.entity.WechatOaMaterial;

/**
 * 素材表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaMaterialService extends IService<WechatOaMaterial> {

    PageResult<WechatOaMaterial> pageMaterials(Long accountId, Byte materialType,
                                               Integer pageNum, Integer pageSize);

    void saveMaterial(WechatOaMaterial material);

    void deleteMaterial(Long id);

    void syncToWechat(Long id);
}
