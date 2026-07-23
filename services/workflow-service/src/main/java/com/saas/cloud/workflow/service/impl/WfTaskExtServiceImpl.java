package com.saas.cloud.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.kafka.producer.KafkaProducerService;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.workflow.api.dto.TaskAddSignDTO;
import com.saas.cloud.workflow.api.dto.TaskApproveDTO;
import com.saas.cloud.workflow.api.dto.TaskDelegateDTO;
import com.saas.cloud.workflow.api.dto.TaskQueryDTO;
import com.saas.cloud.workflow.api.dto.TaskRejectDTO;
import com.saas.cloud.workflow.api.dto.TaskTransferDTO;
import com.saas.cloud.workflow.entity.WfProcessInstanceExt;
import com.saas.cloud.workflow.entity.WfTaskExt;
import com.saas.cloud.workflow.mapper.WfProcessInstanceExtMapper;
import com.saas.cloud.workflow.mapper.WfTaskExtMapper;
import com.saas.cloud.workflow.service.IWfCopyService;
import com.saas.cloud.workflow.service.IWfTaskExtService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务扩展表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfTaskExtServiceImpl
        extends ServiceImpl<WfTaskExtMapper, WfTaskExt>
        implements IWfTaskExtService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final WfProcessInstanceExtMapper processInstanceExtMapper;
    private final IWfCopyService copyService;
    private final KafkaProducerService kafkaProducerService;

    private static final byte ACTION_APPROVE = 1;
    private static final byte ACTION_REJECT = 2;
    private static final byte ACTION_TRANSFER = 3;
    private static final byte ACTION_DELEGATE = 4;
    private static final byte ACTION_ADD_SIGN = 5;

    private static final byte INSTANCE_STATUS_RUNNING = 0;
    private static final byte INSTANCE_STATUS_COMPLETED = 1;
    private static final byte INSTANCE_RESULT_APPROVED = 1;
    private static final byte INSTANCE_RESULT_REJECTED = 2;

    @Override
    public PageResult<WfTaskExt> pageTodoTasks(TaskQueryDTO query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        // 从 Flowable 查待办任务ID列表，按租户隔离避免跨租户扫描 ACT_RU_TASK
        Long tenantId = TenantContext.getTenantId();
        var taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId));
        if (tenantId != null) {
            taskQuery.tenantId(String.valueOf(tenantId));
        }
        List<Task> flowableTasks = taskQuery.orderByTaskCreateTime().desc().list();

        if (flowableTasks.isEmpty()) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        List<String> taskIds = flowableTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<WfTaskExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WfTaskExt::getTaskId, taskIds)
                .isNull(WfTaskExt::getAction)
                .orderByDesc(WfTaskExt::getCreateTime);

        Page<WfTaskExt> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<WfTaskExt> pageDoneTasks(TaskQueryDTO query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        LambdaQueryWrapper<WfTaskExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskExt::getAssigneeId, userId)
                .isNotNull(WfTaskExt::getAction)
                .orderByDesc(WfTaskExt::getCompleteTime);

        Page<WfTaskExt> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(TaskApproveDTO dto) {
        Task task = getAndValidateTask(dto.getTaskId());

        // 如果是委派状态，先 resolve 再 complete
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.resolveTask(dto.getTaskId());
        }

        if (StrUtil.isNotBlank(dto.getComment())) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), "APPROVE", dto.getComment());
        }
        taskService.complete(dto.getTaskId());

        // 更新任务扩展表
        updateTaskExt(dto.getTaskId(), ACTION_APPROVE, dto.getComment());

        // 处理抄送
        handleCopyAfterTask(task, dto.getCopyUserIds());

        // 检查流程是否已结束
        checkAndUpdateProcessStatus(task.getProcessInstanceId(), INSTANCE_RESULT_APPROVED);

        // 通知下一个审批人
        sendNextTaskNotification(task.getProcessInstanceId());

        log.info("审批通过, taskId={}, processInstanceId={}", dto.getTaskId(), task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(TaskRejectDTO dto) {
        Task task = getAndValidateTask(dto.getTaskId());

        if (StrUtil.isNotBlank(dto.getComment())) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), "REJECT", dto.getComment());
        }

        // 驳回直接终止流程
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(),
                StrUtil.isBlank(dto.getComment()) ? "审批驳回" : dto.getComment());

        updateTaskExt(dto.getTaskId(), ACTION_REJECT, dto.getComment());

        // 更新流程实例状态
        LambdaUpdateWrapper<WfProcessInstanceExt> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WfProcessInstanceExt::getProcessInstanceId, task.getProcessInstanceId())
                .set(WfProcessInstanceExt::getStatus, INSTANCE_STATUS_COMPLETED)
                .set(WfProcessInstanceExt::getResult, INSTANCE_RESULT_REJECTED)
                .set(WfProcessInstanceExt::getEndTime, LocalDateTime.now());
        processInstanceExtMapper.update(null, updateWrapper);

        log.info("审批驳回, taskId={}, processInstanceId={}", dto.getTaskId(), task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(TaskTransferDTO dto) {
        Task task = getAndValidateTask(dto.getTaskId());

        if (StrUtil.isNotBlank(dto.getComment())) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), "TRANSFER", dto.getComment());
        }

        // 转办：修改 assignee
        taskService.setAssignee(dto.getTaskId(), String.valueOf(dto.getTargetUserId()));

        updateTaskExt(dto.getTaskId(), ACTION_TRANSFER, dto.getComment());

        // 创建新的任务扩展记录（给新处理人）
        WfTaskExt newTaskExt = new WfTaskExt();
        newTaskExt.setTaskId(dto.getTaskId());
        newTaskExt.setProcessInstanceId(task.getProcessInstanceId());
        newTaskExt.setTaskName(task.getName());
        newTaskExt.setAssigneeId(dto.getTargetUserId());
        save(newTaskExt);

        sendSingleTaskNotification(task.getProcessInstanceId(), dto.getTaskId(),
                task.getName(), String.valueOf(dto.getTargetUserId()));

        log.info("转办成功, taskId={}, targetUserId={}", dto.getTaskId(), dto.getTargetUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(TaskDelegateDTO dto) {
        Task task = getAndValidateTask(dto.getTaskId());

        if (StrUtil.isNotBlank(dto.getComment())) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), "DELEGATE", dto.getComment());
        }

        // 委派：原处理人变为 owner，新处理人变为 assignee
        taskService.delegateTask(dto.getTaskId(), String.valueOf(dto.getTargetUserId()));

        updateTaskExt(dto.getTaskId(), ACTION_DELEGATE, dto.getComment());

        WfTaskExt newTaskExt = new WfTaskExt();
        newTaskExt.setTaskId(dto.getTaskId());
        newTaskExt.setProcessInstanceId(task.getProcessInstanceId());
        newTaskExt.setTaskName(task.getName());
        newTaskExt.setAssigneeId(dto.getTargetUserId());
        newTaskExt.setOwnerId(UserContext.getUserId());
        save(newTaskExt);

        sendSingleTaskNotification(task.getProcessInstanceId(), dto.getTaskId(),
                task.getName(), String.valueOf(dto.getTargetUserId()));

        log.info("委派成功, taskId={}, targetUserId={}", dto.getTaskId(), dto.getTargetUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSign(TaskAddSignDTO dto) {
        Task task = getAndValidateTask(dto.getTaskId());

        if (StrUtil.isNotBlank(dto.getComment())) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), "ADD_SIGN", dto.getComment());
        }

        // 加签：为每个用户创建子任务
        for (Long userId : dto.getUserIds()) {
            Task subTask = taskService.newTask();
            subTask.setName(task.getName() + " (加签)");
            subTask.setAssignee(String.valueOf(userId));
            subTask.setParentTaskId(dto.getTaskId());
            subTask.setTenantId(task.getTenantId());
            if (subTask instanceof TaskEntityImpl) {
                ((TaskEntityImpl) subTask).setProcessInstanceId(task.getProcessInstanceId());
            }
            taskService.saveTask(subTask);

            WfTaskExt taskExt = new WfTaskExt();
            taskExt.setTaskId(subTask.getId());
            taskExt.setProcessInstanceId(task.getProcessInstanceId());
            taskExt.setTaskName(subTask.getName());
            taskExt.setAssigneeId(userId);
            save(taskExt);

            sendSingleTaskNotification(task.getProcessInstanceId(), subTask.getId(),
                    subTask.getName(), String.valueOf(userId));
        }

        // 更新原任务扩展记录
        updateTaskExt(dto.getTaskId(), ACTION_ADD_SIGN, dto.getComment());

        log.info("加签成功, taskId={}, userIds={}", dto.getTaskId(), dto.getUserIds());
    }

    @Override
    public void urge(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已完成");
        }

        // 查询流程实例扩展信息
        LambdaQueryWrapper<WfProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessInstanceExt::getProcessInstanceId, task.getProcessInstanceId())
                .last("LIMIT 1");
        WfProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);

        try {
            NotifyEvent event = new NotifyEvent();
            event.setType((byte) 2);
            event.setTenantId(TenantContext.getTenantId());
            event.setReceiverId(task.getAssignee() != null ? Long.valueOf(task.getAssignee()) : null);
            event.setSenderId(UserContext.getUserId());
            event.setTitle("催办提醒");
            event.setContent(String.format("您有一条待办任务【%s】被催办，请尽快处理",
                    task.getName()));
            event.setBizType("TASK_URGED");
            event.setBizId(task.getId());
            kafkaProducerService.send(KafkaConfig.TOPIC_NOTIFY_EVENT, event);
        } catch (Exception e) {
            log.warn("发送催办通知失败: {}", e.getMessage());
        }

        log.info("催办成功, taskId={}, assignee={}", taskId, task.getAssignee());
    }

    private Task getAndValidateTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已完成");
        }

        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && !String.valueOf(currentUserId).equals(task.getAssignee())) {
            // 委派场景下 owner 也可以操作
            if (task.getOwner() == null || !String.valueOf(currentUserId).equals(task.getOwner())) {
                throw new BusinessException("您不是当前任务的处理人");
            }
        }
        return task;
    }

    private void updateTaskExt(String taskId, byte action, String comment) {
        LambdaUpdateWrapper<WfTaskExt> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WfTaskExt::getTaskId, taskId)
                .isNull(WfTaskExt::getAction)
                .set(WfTaskExt::getAction, action)
                .set(WfTaskExt::getComment, comment)
                .set(WfTaskExt::getCompleteTime, LocalDateTime.now())
                .set(WfTaskExt::getAssigneeId, UserContext.getUserId());
        update(wrapper);
    }

    private void checkAndUpdateProcessStatus(String processInstanceId, byte result) {
        // 检查 Flowable 中流程是否已结束
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            // 流程已结束
            LambdaUpdateWrapper<WfProcessInstanceExt> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WfProcessInstanceExt::getProcessInstanceId, processInstanceId)
                    .eq(WfProcessInstanceExt::getStatus, INSTANCE_STATUS_RUNNING)
                    .set(WfProcessInstanceExt::getStatus, INSTANCE_STATUS_COMPLETED)
                    .set(WfProcessInstanceExt::getResult, result)
                    .set(WfProcessInstanceExt::getEndTime, LocalDateTime.now());
            processInstanceExtMapper.update(null, updateWrapper);
        }
    }

    private void handleCopyAfterTask(Task task, List<Long> copyUserIds) {
        if (copyUserIds == null || copyUserIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<WfProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessInstanceExt::getProcessInstanceId, task.getProcessInstanceId())
                .last("LIMIT 1");
        WfProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        if (instanceExt != null) {
            copyService.createCopies(task.getProcessInstanceId(), instanceExt.getProcessName(),
                    instanceExt.getTitle(), instanceExt.getInitiatorId(), instanceExt.getInitiatorName(),
                    task.getName(), copyUserIds);
        }
    }

    private void sendNextTaskNotification(String processInstanceId) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            LambdaQueryWrapper<WfProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfProcessInstanceExt::getProcessInstanceId, processInstanceId)
                    .last("LIMIT 1");
            WfProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);

            for (Task task : tasks) {
                if (task.getAssignee() != null) {
                    sendSingleTaskNotification(processInstanceId, task.getId(),
                            task.getName(), task.getAssignee());
                }
            }
        } catch (Exception e) {
            log.warn("发送下一节点审批通知失败: {}", e.getMessage());
        }
    }

    private void sendSingleTaskNotification(String processInstanceId, String taskId,
                                            String taskName, String assigneeId) {
        try {
            NotifyEvent event = new NotifyEvent();
            event.setType((byte) 1);
            event.setTenantId(TenantContext.getTenantId());
            event.setReceiverId(assigneeId != null ? Long.valueOf(assigneeId) : null);
            event.setTitle("待办任务通知");
            event.setContent(String.format("您有一条新的待办任务【%s】，请及时处理", taskName));
            event.setBizType("TASK_ASSIGNED");
            event.setBizId(taskId);
            kafkaProducerService.send(KafkaConfig.TOPIC_NOTIFY_EVENT, event);
        } catch (Exception e) {
            log.warn("发送任务通知失败: {}", e.getMessage());
        }
    }
}
