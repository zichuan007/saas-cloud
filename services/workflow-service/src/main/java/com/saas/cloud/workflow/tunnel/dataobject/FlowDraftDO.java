package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FlowDraft DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_draft")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowDraftDO extends TenantBaseEntity {

    private String processDefKey;
    private String processName;
    private String businessKey;
    private String draftContent;
    private String status;
}