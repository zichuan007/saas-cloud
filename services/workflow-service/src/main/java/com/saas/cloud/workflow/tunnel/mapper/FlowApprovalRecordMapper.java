package com.saas.cloud.workflow.tunnel.mapper;

import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlowApprovalRecordMapper extends BaseMapper<FlowApprovalRecordDO> {
    List<FlowApprovalRecordDO> selectByOperatorId(@Param("operatorId") Long operatorId);
    List<FlowApprovalRecordDO> selectByActionTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
