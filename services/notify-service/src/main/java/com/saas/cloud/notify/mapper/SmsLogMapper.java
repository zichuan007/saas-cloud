package com.saas.cloud.notify.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.notify.entity.SmsLog;

/**
 * 短信发送日志 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {

}
