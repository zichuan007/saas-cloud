package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FlowTaskRelation DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_task_relation")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTaskRelationDO extends TenantBaseEntity {

    private String processInstanceId;
    private String taskId;
    private String parentTaskId;
    private String relationType;
    private Long operatorId;
    private String operatorName;
    private Long targetUserId;
    private String targetUserName;
}