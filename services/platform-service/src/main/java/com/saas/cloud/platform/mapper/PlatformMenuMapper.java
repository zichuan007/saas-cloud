package com.saas.cloud.platform.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.platform.entity.PlatformMenu;

/**
 * 平台菜单表 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-06-06
 */
@Mapper
public interface PlatformMenuMapper extends BaseMapper<PlatformMenu> {

}
