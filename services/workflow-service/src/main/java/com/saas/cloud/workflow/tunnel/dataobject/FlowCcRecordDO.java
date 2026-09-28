package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 抄送记录 DO — 映射 wf_copy 表
 */
@TableName("wf_copy")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowCcRecordDO extends TenantBaseEntity {
    private String processInstanceId;
    private String processName;
    private String title;
    private Long initiatorId;
    private String initiatorName;
    private Long receiverId;
    private String receiverName;
    private String taskName;
    private Integer isRead;
    private LocalDateTime readTime;
}