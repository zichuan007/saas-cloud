package com.saas.cloud.rbac.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.OperationLog;

/**
 * 操作日志表 Mapper 接口
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
