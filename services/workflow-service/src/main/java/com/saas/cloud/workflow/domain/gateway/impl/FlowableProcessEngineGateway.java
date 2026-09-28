package com.saas.cloud.workflow.domain.gateway.impl;

import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Flowable 流程引擎防腐层实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FlowableProcessEngineGateway implements ProcessEngineGateway {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final ManagementService managementService;

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    @Override
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    @Override
    public void delegateTask(String taskId, String delegateUserId) {
        taskService.delegateTask(taskId, delegateUserId);
    }

    @Override
    public void setAssignee(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    @Override
    public void addCandidateUser(String taskId, String userId) {
        taskService.addCandidateUser(taskId, userId);
    }

    @Override
    public void deleteCandidateUser(String taskId, String userId) {
        taskService.deleteCandidateUser(taskId, userId);
    }

    @Override
    public Map<String, Object> getVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    public void setVariable(String processInstanceId, String name, Object value) {
        runtimeService.setVariable(processInstanceId, name, value);
    }

    @Override
    public void setVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    public Object getVariable(String processInstanceId, String name) {
        return runtimeService.getVariable(processInstanceId, name);
    }

    @Override
    public boolean hasVariable(String processInstanceId, String name) {
        return runtimeService.hasVariable(processInstanceId, name);
    }

    @Override
    public String startProcessInstance(String processDefinitionId, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceById(processDefinitionId, variables).getId();
    }

    @Override
    public void deleteProcessInstance(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    @Override
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    public List<String> getActiveActivityIds(String processInstanceId) {
        return runtimeService.getActiveActivityIds(processInstanceId);
    }

    @Override
    public Map<String, Object> queryHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult() != null ? Map.of() : Map.of();
    }

    @Override
    public List<Map<String, Object>> queryHistoricActivityInstances(String processInstanceId) {
        List<Map<String, Object>> result = new ArrayList<>();
        historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list()
                .forEach(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("activityId", a.getActivityId());
                    m.put("activityName", a.getActivityName());
                    m.put("activityType", a.getActivityType());
                    m.put("assignee", a.getAssignee());
                    m.put("startTime", a.getStartTime());
                    m.put("endTime", a.getEndTime());
                    m.put("durationInMillis", a.getDurationInMillis());
                    result.add(m);
                });
        return result;
    }

    @Override
    public List<Map<String, Object>> queryHistoricTaskInstances(String processInstanceId) {
        List<Map<String, Object>> result = new ArrayList<>();
        historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list()
                .forEach(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("taskId", t.getId());
                    m.put("taskName", t.getName());
                    m.put("taskDefinitionKey", t.getTaskDefinitionKey());
                    m.put("assignee", t.getAssignee());
                    m.put("owner", t.getOwner());
                    m.put("startTime", t.getStartTime());
                    m.put("endTime", t.getEndTime());
                    m.put("durationInMillis", t.getDurationInMillis());
                    result.add(m);
                });
        return result;
    }

    @Override
    public Map<String, Object> getProcessDefinition(String processDefinitionId) {
        var pd = repositoryService.getProcessDefinition(processDefinitionId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pd.getId());
        m.put("key", pd.getKey());
        m.put("name", pd.getName());
        m.put("version", pd.getVersion());
        m.put("isSuspended", pd.isSuspended());
        return m;
    }

    @Override
    public String getProcessModel(String processDefinitionId) {
        try {
            byte[] bytes = repositoryService.getProcessModel(processDefinitionId);
            return bytes != null ? new String(bytes) : null;
        } catch (Exception e) {
            log.warn("获取流程模型失败: {}", processDefinitionId, e);
            return null;
        }
    }

    @Override
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    @Override
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    @Override
    public void moveActivityIdTo(String fromActivityId, String toActivityId) {
        runtimeService.createChangeActivityStateBuilder()
                .moveActivityIdTo(fromActivityId, toActivityId)
                .changeState();
    }

    @Override
    public boolean validateActivityJumpAllowed(String fromActivityId, String toActivityId) {
        // Flowable 7.x 默认允许跳转，具体业务校验在上层
        return true;
    }

    @Override
    public void addComment(String taskId, String processInstanceId, String comment) {
        taskService.addComment(taskId, processInstanceId, comment);
    }

    @Override
    public List<Map<String, Object>> getComments(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>();
        taskService.getTaskComments(taskId).forEach(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("userId", c.getUserId());
            m.put("message", c.getFullMessage());
            m.put("time", c.getTime());
            result.add(m);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> getIdentityLinksForTask(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>();
        taskService.getIdentityLinksForTask(taskId).forEach(il -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", il.getUserId());
            m.put("groupId", il.getGroupId());
            m.put("type", il.getType());
            result.add(m);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> queryActiveTasks(String processInstanceId) {
        List<Map<String, Object>> result = new ArrayList<>();
        taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list()
                .forEach(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("taskId", t.getId());
                    m.put("taskName", t.getName());
                    m.put("taskDefinitionKey", t.getTaskDefinitionKey());
                    m.put("assignee", t.getAssignee());
                    m.put("owner", t.getOwner());
                    m.put("createTime", t.getCreateTime());
                    result.add(m);
                });
        return result;
    }

    @Override
    public Map<String, Object> queryTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", task.getId());
        m.put("taskName", task.getName());
        m.put("taskDefinitionKey", task.getTaskDefinitionKey());
        m.put("assignee", task.getAssignee());
        m.put("owner", task.getOwner());
        m.put("processInstanceId", task.getProcessInstanceId());
        m.put("processDefinitionId", task.getProcessDefinitionId());
        m.put("createTime", task.getCreateTime());
        return m;
    }
}