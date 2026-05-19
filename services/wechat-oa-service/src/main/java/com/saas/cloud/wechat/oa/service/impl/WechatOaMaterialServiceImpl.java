package com.saas.cloud.wechat.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaMaterial;
import com.saas.cloud.wechat.oa.mapper.WechatOaMaterialMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 素材表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaMaterialServiceImpl
        extends ServiceImpl<WechatOaMaterialMapper, WechatOaMaterial>
        implements IWechatOaMaterialService {

    private static final String[] MATERIAL_TYPE_NAMES = {"image", "voice", "video", "thumb"};

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Override
    public PageResult<WechatOaMaterial> pageMaterials(Long accountId, Byte materialType,
                                                      Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WechatOaMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatOaMaterial::getAccountId, accountId)
                .eq(materialType != null, WechatOaMaterial::getMaterialType, materialType)
                .orderByDesc(WechatOaMaterial::getCreateTime);

        Page<WechatOaMaterial> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void saveMaterial(WechatOaMaterial material) {
        save(material);
        log.info("保存素材记录, id={}, fileName={}", material.getId(), material.getFileName());
    }

    @Override
    public void deleteMaterial(Long id) {
        WechatOaMaterial existing = getById(id);
        if (existing == null) {
            throw new BusinessException("素材不存在");
        }
        removeById(id);
        log.info("删除素材, id={}, fileName={}", id, existing.getFileName());
    }

    @Override
    public void syncToWechat(Long id) {
        WechatOaMaterial material = getById(id);
        if (material == null) {
            throw new BusinessException("素材不存在");
        }

        String accessToken = accountService.getValidAccessToken(material.getAccountId());
        int typeIdx = material.getMaterialType() != null ? material.getMaterialType() : 0;
        String typeName = typeIdx < MATERIAL_TYPE_NAMES.length
                ? MATERIAL_TYPE_NAMES[typeIdx] : "image";

        Map<String, Object> result = wechatApiClient.addMaterial(
                accessToken, typeName, null, material.getFileName());
        String mediaId = (String) result.get("media_id");
        String wechatUrl = (String) result.get("url");

        material.setMediaId(mediaId);
        if (wechatUrl != null) {
            material.setWechatUrl(wechatUrl);
        }
        updateById(material);
        log.info("素材同步到微信成功, id={}, mediaId={}", id, mediaId);
    }
}
