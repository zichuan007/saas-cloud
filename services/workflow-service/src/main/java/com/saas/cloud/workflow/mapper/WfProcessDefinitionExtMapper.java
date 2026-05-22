package com.saas.cloud.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;

/**
 * 流程定义扩展表 Mapper 接口
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface WfProcessDefinitionExtMapper extends BaseMapper<WfProcessDefinitionExt> {

}
