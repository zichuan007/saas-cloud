package com.saas.cloud.workflow.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.kafka.producer.KafkaProducerService;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.workflow.api.dto.ProcessQueryDTO;
import com.saas.cloud.workflow.api.dto.ProcessStartDTO;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;
import com.saas.cloud.workflow.entity.WfProcessInstanceExt;
import com.saas.cloud.workflow.entity.WfTaskExt;
import com.saas.cloud.workflow.mapper.WfProcessInstanceExtMapper;
import com.saas.cloud.workflow.mapper.WfTaskExtMapper;
import com.saas.cloud.workflow.service.IWfCopyService;
import com.saas.cloud.workflow.service.IWfProcessDefinitionExtService;
import com.saas.cloud.workflow.service.IWfProcessInstanceExtService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 流程实例扩展表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfProcessInstanceExtServiceImpl
        extends ServiceImpl<WfProcessInstanceExtMapper, WfProcessInstanceExt>
        implements IWfProcessInstanceExtService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final IWfProcessDefinitionExtService processDefinitionExtService;
    private final IWfCopyService copyService;
    private final WfTaskExtMapper taskExtMapper;
    private final KafkaProducerService kafkaProducerService;

    private static final byte STATUS_RUNNING = 0;
    private static final byte STATUS_COMPLETED = 1;
    private static final byte STATUS_CANCELLED = 2;
    private static final byte STATUS_TERMINATED = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @com.saas.cloud.common.redis.lock.DistributedLock(key = "'wf:start:' + #dto.processDefinitionExtId", waitTime = 5, leaseTime = 30)
    public WfProcessInstanceExt startProcess(ProcessStartDTO dto) {
        WfProcessDefinitionExt defExt = processDefinitionExtService.getById(dto.getProcessDefinitionExtId());
        if (defExt == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (StrUtil.isBlank(defExt.getProcessDefinitionId())) {
            throw new BusinessException("流程尚未部署，无法发起");
        }
        if (defExt.getStatus() == null || defExt.getStatus() == 0) {
            throw new BusinessException("流程已挂起，无法发起新实例");
        }

        UserContext.UserInfo currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        Long tenantId = TenantContext.getTenantId();
        String tenantIdStr = tenantId != null ? String.valueOf(tenantId) : "";

        Map<String, Object> variables = new HashMap<>(4);
        variables.put("initiator", String.valueOf(currentUser.getUserId()));
        variables.put("tenantId", tenantIdStr);

        ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                defExt.getProcessDefinitionId(),
                dto.getTitle(),
                variables
        );

        // 保存流程实例扩展记录
        WfProcessInstanceExt instanceExt = new WfProcessInstanceExt();
        instanceExt.setProcessInstanceId(processInstance.getId());
        instanceExt.setProcessDefinitionId(defExt.getProcessDefinitionId());
        instanceExt.setProcessKey(defExt.getProcessKey());
        instanceExt.setProcessName(defExt.getProcessName());
        instanceExt.setTitle(dto.getTitle());
        instanceExt.setInitiatorId(currentUser.getUserId());
        instanceExt.setInitiatorName(currentUser.getUsername());
        instanceExt.setInitiatorDeptId(currentUser.getDeptId());
        instanceExt.setFormData(dto.getFormData());
        instanceExt.setStatus(STATUS_RUNNING);
        save(instanceExt);

        // 记录首个任务到扩展表
        List<Task> firstTasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();
        for (Task task : firstTasks) {
            WfTaskExt taskExt = new WfTaskExt();
            taskExt.setTaskId(task.getId());
            taskExt.setProcessInstanceId(processInstance.getId());
            taskExt.setTaskName(task.getName());
            if (task.getAssignee() != null) {
                try {
                    taskExt.setAssigneeId(Long.valueOf(task.getAssignee()));
                } catch (NumberFormatException ignored) {
                }
            }
            taskExtMapper.insert(taskExt);
        }

        // 处理抄送
        if (dto.getCopyUserIds() != null && !dto.getCopyUserIds().isEmpty()) {
            String taskName = firstTasks.isEmpty() ? "发起" : firstTasks.get(0).getName();
            copyService.createCopies(processInstance.getId(), defExt.getProcessName(),
                    dto.getTitle(), currentUser.getUserId(), currentUser.getUsername(),
                    taskName, dto.getCopyUserIds());
        }

        sendTaskNotification(processInstance.getId(), defExt.getProcessName(), dto.getTitle());

        log.info("发起流程成功, processInstanceId={}, processKey={}",
                processInstance.getId(), defExt.getProcessKey());
        return instanceExt;
    }

    @Override
    public PageResult<WfProcessInstanceExt> pageMyInitiated(ProcessQueryDTO query) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<WfProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessInstanceExt::getInitiatorId, userId)
                .like(StrUtil.isNotBlank(query.getProcessName()),
                        WfProcessInstanceExt::getProcessName, query.getProcessName())
                .eq(query.getStatus() != null, WfProcessInstanceExt::getStatus, query.getStatus())
                .orderByDesc(WfProcessInstanceExt::getCreateTime);

        Page<WfProcessInstanceExt> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public Map<String, Object> getProcessDetail(Long id) {
        WfProcessInstanceExt instanceExt = getById(id);
        if (instanceExt == null) {
            throw new BusinessException("流程实例不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>(8);
        result.put("instance", instanceExt);

        // 审批时间线
        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceExt.getProcessInstanceId())
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        List<Map<String, Object>> timeline = activityList.stream()
                .filter(act -> "userTask".equals(act.getActivityType())
                        || "startEvent".equals(act.getActivityType()))
                .map(act -> {
                    Map<String, Object> item = new LinkedHashMap<>(8);
                    item.put("activityId", act.getActivityId());
                    item.put("activityName", act.getActivityName());
                    item.put("activityType", act.getActivityType());
                    item.put("assignee", act.getAssignee());
                    item.put("startTime", act.getStartTime());
                    item.put("endTime", act.getEndTime());
                    if (act.getEndTime() != null && act.getStartTime() != null) {
                        item.put("durationMs", act.getEndTime().getTime() - act.getStartTime().getTime());
                    }
                    return item;
                })
                .collect(Collectors.toList());
        result.put("timeline", timeline);

        // 关联的任务审批记录
        LambdaQueryWrapper<WfTaskExt> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(WfTaskExt::getProcessInstanceId, instanceExt.getProcessInstanceId())
                .orderByAsc(WfTaskExt::getCreateTime);
        result.put("tasks", taskExtMapper.selectList(taskWrapper));

        return result;
    }

    @Override
    public Map<String, Object> getProcessDiagram(Long id) {
        WfProcessInstanceExt instanceExt = getById(id);
        if (instanceExt == null) {
            throw new BusinessException("流程实例不存在");
        }

        Map<String, Object> result = new HashMap<>(4);

        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceExt.getProcessInstanceId())
                .list();

        List<String> finishedNodes = activityList.stream()
                .filter(act -> act.getEndTime() != null)
                .map(HistoricActivityInstance::getActivityId)
                .collect(Collectors.toList());

        List<String> activeNodes = new ArrayList<>(activityList.stream()
                .filter(act -> act.getEndTime() == null)
                .map(HistoricActivityInstance::getActivityId)
                .collect(Collectors.toList()));

        if (instanceExt.getStatus() == STATUS_RUNNING) {
            List<Execution> executions = runtimeService.createExecutionQuery()
                    .processInstanceId(instanceExt.getProcessInstanceId())
                    .list();
            for (Execution execution : executions) {
                if (execution.getActivityId() != null && !activeNodes.contains(execution.getActivityId())) {
                    activeNodes.add(execution.getActivityId());
                }
            }
        }

        result.put("finishedNodes", finishedNodes);
        result.put("activeNodes", activeNodes);
        result.put("processDefinitionId", instanceExt.getProcessDefinitionId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(Long id) {
        WfProcessInstanceExt instanceExt = getById(id);
        if (instanceExt == null) {
            throw new BusinessException("流程实例不存在");
        }
        if (instanceExt.getStatus() != STATUS_RUNNING) {
            throw new BusinessException("只有进行中的流程才能撤回");
        }

        Long currentUserId = UserContext.getUserId();
        if (!instanceExt.getInitiatorId().equals(currentUserId)) {
            throw new BusinessException("只有发起人才能撤回流程");
        }

        List<Task> currentTasks = taskService.createTaskQuery()
                .processInstanceId(instanceExt.getProcessInstanceId())
                .list();
        if (currentTasks.isEmpty()) {
            throw new BusinessException("当前流程无可用任务，无法撤回");
        }

        runtimeService.deleteProcessInstance(instanceExt.getProcessInstanceId(), "发起人撤回");

        instanceExt.setStatus(STATUS_CANCELLED);
        instanceExt.setEndTime(LocalDateTime.now());
        if (instanceExt.getCreateTime() != null) {
            instanceExt.setDuration(ChronoUnit.MILLIS.between(instanceExt.getCreateTime(), instanceExt.getEndTime()));
        }
        updateById(instanceExt);

        log.info("撤回流程成功, id={}, processInstanceId={}", id, instanceExt.getProcessInstanceId());
    }

    @Override
    public PageResult<WfProcessInstanceExt> pageRunningInstances(ProcessQueryDTO query) {
        LambdaQueryWrapper<WfProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessInstanceExt::getStatus, STATUS_RUNNING)
                .like(StrUtil.isNotBlank(query.getProcessName()),
                        WfProcessInstanceExt::getProcessName, query.getProcessName())
                .orderByDesc(WfProcessInstanceExt::getCreateTime);

        Page<WfProcessInstanceExt> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateProcess(Long id, String reason) {
        WfProcessInstanceExt instanceExt = getById(id);
        if (instanceExt == null) {
            throw new BusinessException("流程实例不存在");
        }
        if (instanceExt.getStatus() != STATUS_RUNNING) {
            throw new BusinessException("只有进行中的流程才能终止");
        }

        runtimeService.deleteProcessInstance(instanceExt.getProcessInstanceId(),
                StrUtil.isBlank(reason) ? "管理员强制终止" : reason);

        instanceExt.setStatus(STATUS_TERMINATED);
        instanceExt.setEndTime(LocalDateTime.now());
        if (instanceExt.getCreateTime() != null) {
            instanceExt.setDuration(ChronoUnit.MILLIS.between(instanceExt.getCreateTime(), instanceExt.getEndTime()));
        }
        updateById(instanceExt);

        log.info("强制终止流程, id={}, processInstanceId={}, reason={}",
                id, instanceExt.getProcessInstanceId(), reason);
    }

    private void sendTaskNotification(String processInstanceId, String processName, String title) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (Task task : tasks) {
                if (task.getAssignee() != null) {
                    NotifyEvent event = new NotifyEvent();
                    event.setType((byte) 1);
                    event.setTenantId(TenantContext.getTenantId());
                    event.setReceiverId(Long.valueOf(task.getAssignee()));
                    event.setTitle("待办任务通知");
                    event.setContent(String.format("您有一条新的待办任务【%s】（%s - %s），请及时处理",
                            task.getName(), processName, title));
                    event.setBizType("TASK_ASSIGNED");
                    event.setBizId(task.getId());
                    kafkaProducerService.send(KafkaConfig.TOPIC_NOTIFY_EVENT, event);
                }
            }
        } catch (Exception e) {
            log.warn("发送任务通知事件失败: {}", e.getMessage());
        }
    }
}
