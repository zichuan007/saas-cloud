package com.saas.cloud.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.ExportTask;

import org.apache.ibatis.annotations.Mapper;

/**
 * 导出任务 Mapper
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {
}
