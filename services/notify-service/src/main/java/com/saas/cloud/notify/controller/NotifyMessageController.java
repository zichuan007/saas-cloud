package com.saas.cloud.notify.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.notify.api.vo.MessageVO;
import com.saas.cloud.notify.service.INotifyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 站内消息 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotifyMessageController {

    private final INotifyMessageService messageService;

    /**
     * 分页查询消息列表
     *
     * @param type     消息类型（可选）
     * @param isRead   已读状态（可选）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResult<PageResult<MessageVO>> list(
            @RequestParam(required = false) Byte type,
            @RequestParam(required = false) Byte isRead,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long receiverId = UserContext.getUserId();
        return ApiResult.ok(messageService.pageMessages(receiverId, type, isRead, pageNum, pageSize));
    }

    /**
     * 获取未读消息数
     *
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount() {
        Long receiverId = UserContext.getUserId();
        return ApiResult.ok(messageService.getUnreadCount(receiverId));
    }

    /**
     * 标记消息为已读
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    public ApiResult<Void> markAsRead(@PathVariable("id") Long id) {
        messageService.markAsRead(id);
        return ApiResult.ok();
    }

    /**
     * 全部标记已读
     *
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public ApiResult<Void> markAllAsRead() {
        Long receiverId = UserContext.getUserId();
        messageService.markAllAsRead(receiverId);
        return ApiResult.ok();
    }

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        messageService.deleteMessage(id);
        return ApiResult.ok();
    }
}
