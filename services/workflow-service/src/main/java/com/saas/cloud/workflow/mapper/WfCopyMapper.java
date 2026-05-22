package com.saas.cloud.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.workflow.entity.WfCopy;

/**
 * 流程抄送表 Mapper 接口
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface WfCopyMapper extends BaseMapper<WfCopy> {

}
