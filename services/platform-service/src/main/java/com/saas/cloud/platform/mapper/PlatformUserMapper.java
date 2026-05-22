package com.saas.cloud.platform.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.platform.entity.PlatformUser;

/**
 * 平台用户表 Mapper 接口
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface PlatformUserMapper extends BaseMapper<PlatformUser> {

}
