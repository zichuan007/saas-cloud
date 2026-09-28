package com.saas.cloud.workflow.domain.gateway;

import java.util.List;
import java.util.Map;

/**
 * 流程引擎防腐层接口 — 封装 Flowable 全部核心操作，领域层零 Flowable 类型依赖
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public interface ProcessEngineGateway {

    // ========== 任务操作 ==========
    void completeTask(String taskId, Map<String, Object> variables);
    void claimTask(String taskId, String userId);
    void delegateTask(String taskId, String delegateUserId);
    void setAssignee(String taskId, String userId);
    void addCandidateUser(String taskId, String userId);
    void deleteCandidateUser(String taskId, String userId);

    // ========== 流程变量 ==========
    Map<String, Object> getVariables(String processInstanceId);
    void setVariable(String processInstanceId, String name, Object value);
    void setVariables(String processInstanceId, Map<String, Object> variables);
    Object getVariable(String processInstanceId, String name);
    boolean hasVariable(String processInstanceId, String name);

    // ========== 流程实例 ==========
    String startProcessInstance(String processDefinitionId, Map<String, Object> variables);
    void deleteProcessInstance(String processInstanceId, String reason);
    void suspendProcessInstance(String processInstanceId);
    void activateProcessInstance(String processInstanceId);
    List<String> getActiveActivityIds(String processInstanceId);

    // ========== 历史查询 ==========
    Map<String, Object> queryHistoricProcessInstance(String processInstanceId);
    List<Map<String, Object>> queryHistoricActivityInstances(String processInstanceId);
    List<Map<String, Object>> queryHistoricTaskInstances(String processInstanceId);

    // ========== 流程定义 ==========
    Map<String, Object> getProcessDefinition(String processDefinitionId);
    String getProcessModel(String processDefinitionId);
    void suspendProcessDefinition(String processDefinitionId);
    void activateProcessDefinition(String processDefinitionId);

    // ========== 节点跳转 ==========
    void moveActivityIdTo(String fromActivityId, String toActivityId);
    boolean validateActivityJumpAllowed(String fromActivityId, String toActivityId);

    // ========== 评论 ==========
    void addComment(String taskId, String processInstanceId, String comment);
    List<Map<String, Object>> getComments(String taskId);

    // ========== 身份链接 ==========
    List<Map<String, Object>> getIdentityLinksForTask(String taskId);

    // ========== 任务查询 ==========
    List<Map<String, Object>> queryActiveTasks(String processInstanceId);
    Map<String, Object> queryTask(String taskId);
}