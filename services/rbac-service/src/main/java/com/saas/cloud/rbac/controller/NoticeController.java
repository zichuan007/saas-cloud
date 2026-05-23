package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.rbac.api.dto.NoticeCreateDTO;
import com.saas.cloud.rbac.api.vo.NoticeVO;
import com.saas.cloud.rbac.service.INoticeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 通知公告
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeController {

    private final INoticeService noticeService;

    /**
     * 分页查询公告（管理端）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    标题关键字
     * @return 公告分页
     */
    @Operation(summary = "分页查询公告")
    @GetMapping("/list")
    public ApiResult<PageResult<NoticeVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title) {
        return ApiResult.ok(noticeService.pageNotice(pageNum, pageSize, title));
    }

    /**
     * 查询已发布公告（用户端，含已读状态）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 公告分页
     */
    @Operation(summary = "查询已发布公告")
    @GetMapping("/published")
    public ApiResult<PageResult<NoticeVO>> published(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResult.ok(noticeService.pagePublished(pageNum, pageSize, UserContext.getUserId()));
    }

    /**
     * 创建公告
     *
     * @param dto 创建请求
     * @return 操作结果
     */
    @Operation(summary = "创建公告")
    @OperationLog(module = "通知公告", operation = "创建公告")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody NoticeCreateDTO dto) {
        noticeService.create(dto);
        return ApiResult.ok();
    }

    /**
     * 更新公告
     *
     * @param id  公告ID
     * @param dto 更新内容
     * @return 操作结果
     */
    @Operation(summary = "更新公告")
    @OperationLog(module = "通知公告", operation = "更新公告")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeCreateDTO dto) {
        noticeService.update(id, dto);
        return ApiResult.ok();
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @Operation(summary = "发布公告")
    @OperationLog(module = "通知公告", operation = "发布公告")
    @PutMapping("/{id}/publish")
    public ApiResult<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return ApiResult.ok();
    }

    /**
     * 撤回公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @Operation(summary = "撤回公告")
    @OperationLog(module = "通知公告", operation = "撤回公告")
    @PutMapping("/{id}/revoke")
    public ApiResult<Void> revoke(@PathVariable Long id) {
        noticeService.revoke(id);
        return ApiResult.ok();
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @Operation(summary = "删除公告")
    @OperationLog(module = "通知公告", operation = "删除公告")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResult.ok();
    }

    /**
     * 标记公告为已读
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id, UserContext.getUserId());
        return ApiResult.ok();
    }

    /**
     * 查询未读公告数
     *
     * @return 未读数量
     */
    @Operation(summary = "查询未读公告数")
    @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount() {
        return ApiResult.ok(noticeService.countUnread(UserContext.getUserId()));
    }
}
