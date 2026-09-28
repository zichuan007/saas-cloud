package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例扩展 DO
 */
@TableName("wf_process_instance_ext")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowProcessInstanceExtDO extends TenantBaseEntity {
    private String processInstanceId;
    private String processDefinitionId;
    private String processKey;
    private String processName;
    private String title;
    private Long initiatorId;
    private String initiatorName;
    private Long initiatorDeptId;
    private String businessKey;
    private String flowType;
    private String formData;
    private Integer status;
    private Integer result;
    private LocalDateTime endTime;
    private Long duration;
}