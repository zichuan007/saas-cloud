package com.saas.cloud.workflow.domain.interceptor;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务拦截器上下文
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
public class TaskContext {

    /** 任务ID */
    private String taskId;

    /** 流程实例ID */
    private String processInstanceId;

    /** 节点定义Key */
    private String taskDefKey;

    /** 流程定义Key */
    private String processDefKey;

    /** 当前处理人ID */
    private String assignee;

    /** 发起人ID */
    private Long initiatorId;

    /** 流程变量 */
    private Map<String, Object> variables = new HashMap<>();

    // ====== 控制标志 ======

    /** 跳过后续所有拦截器 */
    private boolean skip;

    /** 跳过当前任务 (自动完成) */
    private boolean skipTask;

    /** 跳过原因 */
    private String skipReason;

    public void skip() {
        this.skip = true;
    }

    public void skipTask() {
        this.skipTask = true;
    }

    public void skipTask(String reason) {
        this.skipTask = true;
        this.skipReason = reason;
    }
}