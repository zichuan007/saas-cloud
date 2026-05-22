package com.saas.cloud.notify.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.notify.entity.NotifyChannelConfig;

/**
 * <p>
 * 租户通知渠道配置表 Mapper 接口
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface NotifyChannelConfigMapper extends BaseMapper<NotifyChannelConfig> {

}
