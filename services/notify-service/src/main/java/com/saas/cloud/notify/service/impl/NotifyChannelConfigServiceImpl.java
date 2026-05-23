package com.saas.cloud.notify.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.notify.api.dto.ChannelConfigUpdateDTO;
import com.saas.cloud.notify.api.enums.NotifyChannelType;
import com.saas.cloud.notify.api.vo.ChannelConfigVO;
import com.saas.cloud.notify.entity.NotifyChannelConfig;
import com.saas.cloud.notify.mapper.NotifyChannelConfigMapper;
import com.saas.cloud.notify.service.INotifyChannelConfigService;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户通知渠道配置表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class NotifyChannelConfigServiceImpl extends ServiceImpl<NotifyChannelConfigMapper, NotifyChannelConfig> implements INotifyChannelConfigService {

    @Override
    public List<ChannelConfigVO> listConfigs() {
        LambdaQueryWrapper<NotifyChannelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(NotifyChannelConfig::getChannelType);
        List<NotifyChannelConfig> configs = this.list(wrapper);

        return configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateConfig(Byte channelType, ChannelConfigUpdateDTO dto) {
        NotifyChannelConfig config = this.lambdaQuery()
                .eq(NotifyChannelConfig::getChannelType, channelType)
                .one();
        if (config == null) {
            // 渠道配置不存在时自动创建
            config = new NotifyChannelConfig();
            config.setChannelType(channelType);
            config.setEnabled(dto.getEnabled());
            config.setConfigJson(dto.getConfigJson());
            this.save(config);
            log.info("[通知中心] 创建渠道配置, channelType={}", channelType);
        } else {
            config.setEnabled(dto.getEnabled());
            config.setConfigJson(dto.getConfigJson());
            this.updateById(config);
            log.info("[通知中心] 更新渠道配置, channelType={}", channelType);
        }
    }

    /**
     * 实体转换为VO
     *
     * @param config 渠道配置实体
     * @return 渠道配置VO
     */
    private ChannelConfigVO convertToVO(NotifyChannelConfig config) {
        ChannelConfigVO vo = new ChannelConfigVO();
        vo.setChannelType(config.getChannelType());
        NotifyChannelType type = NotifyChannelType.getByCode(config.getChannelType());
        vo.setChannelTypeDesc(type != null ? type.getDesc() : "未知");
        vo.setEnabled(config.getEnabled());
        vo.setConfigJson(config.getConfigJson());
        return vo;
    }
}
