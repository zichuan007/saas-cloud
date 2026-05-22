package com.saas.cloud.notify.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.notify.entity.NotifyMessage;

/**
 * <p>
 * 站内消息表 Mapper 接口
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface NotifyMessageMapper extends BaseMapper<NotifyMessage> {

}
