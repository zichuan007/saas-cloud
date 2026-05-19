package com.saas.cloud.notify.service;

import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.notify.api.vo.MessageVO;
import com.saas.cloud.notify.entity.NotifyMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 站内消息表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface INotifyMessageService extends IService<NotifyMessage> {

    /**
     * 分页查询消息列表
     *
     * @param receiverId 接收人ID
     * @param type       消息类型（可选）
     * @param isRead     已读状态（可选）
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    PageResult<MessageVO> pageMessages(Long receiverId, Byte type, Byte isRead, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    Long getUnreadCount(Long receiverId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     */
    void markAsRead(Long messageId);

    /**
     * 标记所有消息为已读
     *
     * @param receiverId 接收人ID
     */
    void markAllAsRead(Long receiverId);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     */
    void deleteMessage(Long messageId);

    /**
     * 根据通知事件创建站内消息（Kafka 消费后调用）
     *
     * @param event 通知事件
     */
    void createMessage(NotifyEvent event);
}
