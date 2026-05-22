package com.saas.cloud.notify.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.notify.api.dto.ChannelConfigUpdateDTO;
import com.saas.cloud.notify.api.vo.ChannelConfigVO;
import com.saas.cloud.notify.entity.NotifyChannelConfig;

/**
 * 租户通知渠道配置表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface INotifyChannelConfigService extends IService<NotifyChannelConfig> {

    /**
     * 查询所有渠道配置
     *
     * @return 渠道配置列表
     */
    List<ChannelConfigVO> listConfigs();

    /**
     * 更新渠道配置
     *
     * @param channelType 渠道类型
     * @param dto         渠道配置更新请求
     */
    void updateConfig(Byte channelType, ChannelConfigUpdateDTO dto);
}
