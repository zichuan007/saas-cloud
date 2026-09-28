package com.saas.cloud.workflow.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ProcessCompleted 领域事件
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String processInstanceId;
    private String processDefKey;
    private String processName;
    private String title;
    private Long initiatorId;
    private String initiatorName;
    private String result;
    private String endReason;
}