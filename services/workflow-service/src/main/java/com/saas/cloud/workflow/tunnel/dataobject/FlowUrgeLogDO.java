package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FlowUrgeLog DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_urge_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowUrgeLogDO extends TenantBaseEntity {

    private String processInstanceId;
    private String taskId;
    private Long urgeUserId;
    private String urgeUserName;
    private Long targetUserId;
    private String targetUserName;
    private LocalDateTime urgeTime;
    private String channel;
}