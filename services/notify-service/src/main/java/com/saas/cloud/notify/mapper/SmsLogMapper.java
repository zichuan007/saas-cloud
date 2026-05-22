package com.saas.cloud.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.notify.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

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
