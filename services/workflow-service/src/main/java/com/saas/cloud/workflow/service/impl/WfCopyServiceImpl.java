package com.saas.cloud.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.api.dto.TaskQueryDTO;
import com.saas.cloud.workflow.entity.WfCopy;
import com.saas.cloud.workflow.mapper.WfCopyMapper;
import com.saas.cloud.workflow.service.IWfCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 流程抄送表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfCopyServiceImpl
        extends ServiceImpl<WfCopyMapper, WfCopy>
        implements IWfCopyService {

    @Override
    public PageResult<WfCopy> pageMyCopies(TaskQueryDTO query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        LambdaQueryWrapper<WfCopy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfCopy::getReceiverId, userId)
                .orderByDesc(WfCopy::getCreateTime);

        Page<WfCopy> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        WfCopy copy = getById(id);
        if (copy == null) {
            throw new BusinessException("抄送记录不存在");
        }
        if (!copy.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作此抄送记录");
        }

        LambdaUpdateWrapper<WfCopy> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WfCopy::getId, id)
                .set(WfCopy::getIsRead, (byte) 1)
                .set(WfCopy::getReadTime, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    public void createCopies(String processInstanceId, String processName, String title,
                             Long initiatorId, String initiatorName, String taskName,
                             List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }

        List<WfCopy> copies = receiverIds.stream()
                .distinct()
                .map(receiverId -> {
                    WfCopy copy = new WfCopy();
                    copy.setProcessInstanceId(processInstanceId);
                    copy.setProcessName(processName);
                    copy.setTitle(title);
                    copy.setInitiatorId(initiatorId);
                    copy.setInitiatorName(initiatorName);
                    copy.setReceiverId(receiverId);
                    copy.setTaskName(taskName);
                    copy.setIsRead((byte) 0);
                    return copy;
                })
                .collect(Collectors.toList());

        saveBatch(copies);
        log.info("批量创建抄送记录, processInstanceId={}, receiverCount={}", processInstanceId, copies.size());
    }
}
