package com.saas.cloud.workflow.domain.service.action.support;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalRecordMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowTaskRelationMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowUrgeLogMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowTaskRelationDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowUrgeLogDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public abstract class ApprovalActionSupport {

    @Autowired
    protected ProcessEngineGateway processEngine;

    @Autowired
    protected FlowApprovalRecordMapper approvalRecordMapper;

    @Autowired
    protected FlowTaskRelationMapper taskRelationMapper;

    @Autowired
    protected FlowUrgeLogMapper urgeLogMapper;

    protected Map<String, Object> getAndValidateTask(String taskId) {
        Map<String, Object> task = processEngine.queryTask(taskId);
        if (task == null) throw new IllegalArgumentException("任务不存在: " + taskId);
        return task;
    }

    protected void claimTaskIfNeeded(Map<String, Object> task, String userId) {
        String assignee = (String) task.get("assignee");
        if (assignee == null || assignee.isEmpty()) {
            processEngine.claimTask((String) task.get("taskId"), userId);
        }
    }

    protected FlowApprovalRecordDO buildRecord(String processInstanceId, String taskId,
                                                String taskDefKey, String taskName,
                                                Long operatorId, String operatorName,
                                                String action, String comment) {
        FlowApprovalRecordDO record = new FlowApprovalRecordDO();
        record.setProcessInstanceId(processInstanceId);
        record.setTaskId(taskId);
        record.setTaskDefKey(taskDefKey);
        record.setTaskName(taskName);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setAction(action);
        record.setComment(comment);
        record.setOperatorType("USER");
        record.setActionTime(LocalDateTime.now());
        record.setFlowType("NORMAL");
        record.setIsFinalDecision(0);
        return record;
    }

    protected void saveRecord(FlowApprovalRecordDO record) {
        approvalRecordMapper.insert(record);
    }

    protected void saveTaskRelation(String processInstanceId, String taskId, String parentTaskId,
                                     String relationType, Long operatorId, String operatorName,
                                     Long targetUserId, String targetUserName) {
        FlowTaskRelationDO relation = new FlowTaskRelationDO();
        relation.setProcessInstanceId(processInstanceId);
        relation.setTaskId(taskId);
        relation.setParentTaskId(parentTaskId);
        relation.setRelationType(relationType);
        relation.setOperatorId(operatorId);
        relation.setOperatorName(operatorName);
        relation.setTargetUserId(targetUserId);
        relation.setTargetUserName(targetUserName);
        taskRelationMapper.insert(relation);
    }

    protected void saveUrgeLog(String processInstanceId, String taskId,
                                Long urgeUserId, String urgeUserName,
                                Long targetUserId, String targetUserName) {
        FlowUrgeLogDO urgeLog = new FlowUrgeLogDO();
        urgeLog.setProcessInstanceId(processInstanceId);
        urgeLog.setTaskId(taskId);
        urgeLog.setUrgeUserId(urgeUserId);
        urgeLog.setUrgeUserName(urgeUserName);
        urgeLog.setTargetUserId(targetUserId);
        urgeLog.setTargetUserName(targetUserName);
        urgeLog.setUrgeTime(LocalDateTime.now());
        urgeLog.setChannel("IN_APP");
        urgeLogMapper.insert(urgeLog);
    }
}