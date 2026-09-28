package com.saas.cloud.workflow.tunnel.mapper;

import com.saas.cloud.workflow.tunnel.dataobject.FlowCcRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlowCcRecordMapper extends BaseMapper<FlowCcRecordDO> {
    List<FlowCcRecordDO> selectByReceiverId(@Param("receiverId") Long receiverId);
}
