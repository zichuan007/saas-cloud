package com.saas.cloud.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.notify.api.vo.MessageVO;
import com.saas.cloud.notify.entity.NotifyMessage;
import com.saas.cloud.notify.entity.NotifyTemplate;
import com.saas.cloud.notify.mapper.NotifyMessageMapper;
import com.saas.cloud.notify.service.INotifyMessageService;
import com.saas.cloud.notify.service.INotifyTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内消息表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotifyMessageServiceImpl extends ServiceImpl<NotifyMessageMapper, NotifyMessage> implements INotifyMessageService {

    private final INotifyTemplateService templateService;

    /** 消息类型描述映射 */
    private static final Map<Byte, String> TYPE_DESC_MAP = new HashMap<>();

    static {
        TYPE_DESC_MAP.put((byte) 0, "系统通知");
        TYPE_DESC_MAP.put((byte) 1, "审批通知");
        TYPE_DESC_MAP.put((byte) 2, "催办");
        TYPE_DESC_MAP.put((byte) 3, "公告");
    }

    @Override
    public PageResult<MessageVO> pageMessages(Long receiverId, Byte type, Byte isRead, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotifyMessage::getReceiverId, receiverId)
                .eq(type != null, NotifyMessage::getType, type)
                .eq(isRead != null, NotifyMessage::getIsRead, isRead)
                .orderByDesc(NotifyMessage::getCreateTime);

        Page<NotifyMessage> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        List<MessageVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Long getUnreadCount(Long receiverId) {
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotifyMessage::getReceiverId, receiverId)
                .eq(NotifyMessage::getIsRead, (byte) 0);
        return (long) this.count(wrapper);
    }

    @Override
    public void markAsRead(Long messageId) {
        NotifyMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (message.getIsRead() != null && message.getIsRead() == 1) {
            return;
        }
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotifyMessage::getId, messageId)
                .set(NotifyMessage::getIsRead, (byte) 1)
                .set(NotifyMessage::getReadTime, LocalDateTime.now());
        this.update(wrapper);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotifyMessage::getReceiverId, receiverId)
                .eq(NotifyMessage::getIsRead, (byte) 0)
                .set(NotifyMessage::getIsRead, (byte) 1)
                .set(NotifyMessage::getReadTime, LocalDateTime.now());
        this.update(wrapper);
    }

    @Override
    public void deleteMessage(Long messageId) {
        NotifyMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        this.removeById(messageId);
    }

    @Override
    public void createMessage(NotifyEvent event) {
        NotifyMessage message = new NotifyMessage();
        message.setReceiverId(event.getReceiverId());
        message.setSenderId(event.getSenderId());
        message.setSenderName(event.getSenderName());
        message.setType(event.getType());
        message.setBizType(event.getBizType());
        message.setBizId(event.getBizId());
        message.setJumpUrl(event.getJumpUrl());
        message.setIsRead((byte) 0);

        // 根据模板编码渲染标题和内容
        if (StringUtils.hasText(event.getTemplateCode())) {
            NotifyTemplate template = templateService.lambdaQuery()
                    .eq(NotifyTemplate::getTemplateCode, event.getTemplateCode())
                    .eq(NotifyTemplate::getStatus, (byte) 1)
                    .one();
            if (template != null) {
                message.setTitle(renderTemplate(template.getTitleTemplate(), event.getParams()));
                message.setContent(renderTemplate(template.getContentTemplate(), event.getParams()));
            } else {
                log.warn("[通知中心] 模板编码 {} 不存在或已禁用，使用事件原始内容", event.getTemplateCode());
                message.setTitle(event.getTitle());
                message.setContent(event.getContent());
            }
        } else {
            // 无模板编码，直接使用事件中的标题和内容
            message.setTitle(event.getTitle());
            message.setContent(event.getContent());
        }

        this.save(message);
        log.info("[通知中心] 创建站内消息成功, receiverId={}, title={}", event.getReceiverId(), message.getTitle());
    }

    /**
     * 渲染模板，将 ${key} 替换为实际值
     *
     * @param template 模板字符串
     * @param params   参数映射
     * @return 渲染后的字符串
     */
    private String renderTemplate(String template, Map<String, String> params) {
        if (!StringUtils.hasText(template)) {
            return template;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    /**
     * 实体转换为VO
     *
     * @param message 消息实体
     * @return 消息VO
     */
    private MessageVO convertToVO(NotifyMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setTypeDesc(TYPE_DESC_MAP.getOrDefault(message.getType(), "未知"));
        vo.setSenderName(message.getSenderName());
        vo.setBizType(message.getBizType());
        vo.setBizId(message.getBizId());
        vo.setJumpUrl(message.getJumpUrl());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
