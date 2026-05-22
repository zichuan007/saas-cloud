package com.saas.cloud.rbac.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.DictData;

/**
 * 字典数据 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {
}
