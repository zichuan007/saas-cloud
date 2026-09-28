package com.saas.cloud.workflow.tunnel.mapper;

import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessInstanceExtDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlowProcessInstanceExtMapper extends BaseMapper<FlowProcessInstanceExtDO> {
    List<FlowProcessInstanceExtDO> selectByInitiatorId(@Param("initiatorId") Long initiatorId);
    List<FlowProcessInstanceExtDO> selectByStatus(@Param("status") Integer status);
    int countByStatus(@Param("status") Integer status);
}
