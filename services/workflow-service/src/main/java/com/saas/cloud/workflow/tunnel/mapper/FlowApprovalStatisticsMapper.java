package com.saas.cloud.workflow.tunnel.mapper;

import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalStatisticsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlowApprovalStatisticsMapper extends BaseMapper<FlowApprovalStatisticsDO> {
    List<FlowApprovalStatisticsDO> selectByUserId(@Param("userId") Long userId);
    List<FlowApprovalStatisticsDO> selectByUserIdAndPeriod(@Param("userId") Long userId, @Param("statPeriod") String statPeriod);
}
