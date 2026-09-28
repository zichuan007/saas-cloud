package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FlowApprovalStatistics DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_approval_statistics")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowApprovalStatisticsDO extends TenantBaseEntity {

    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private String processDefKey;
    private String processName;
    private String statPeriod;
    private LocalDate statDate;
    private Integer totalCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private Integer transferredCount;
    private Integer delegatedCount;
    private Long avgDurationMs;
    private Long maxDurationMs;
    private Long minDurationMs;
}