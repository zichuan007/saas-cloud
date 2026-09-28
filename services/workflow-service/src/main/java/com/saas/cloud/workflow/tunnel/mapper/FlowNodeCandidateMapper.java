package com.saas.cloud.workflow.tunnel.mapper;

import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeCandidateDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlowNodeCandidateMapper extends BaseMapper<FlowNodeCandidateDO> {
    List<FlowNodeCandidateDO> selectByNodeConfigId(@Param("nodeConfigId") Long nodeConfigId);
    List<FlowNodeCandidateDO> selectByProcessDefKeyAndNodeDefKey(@Param("processDefKey") String processDefKey, @Param("nodeDefKey") String nodeDefKey);
}
