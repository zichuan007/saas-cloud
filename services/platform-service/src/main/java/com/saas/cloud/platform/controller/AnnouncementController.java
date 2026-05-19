package com.saas.cloud.platform.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.dto.AnnouncementCreateDTO;
import com.saas.cloud.platform.api.dto.AnnouncementQueryDTO;
import com.saas.cloud.platform.entity.Announcement;
import com.saas.cloud.platform.service.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统公告控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AnnouncementController {

    private final IAnnouncementService announcementService;

    /**
     * 分页查询公告列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResult<PageResult<Announcement>> list(AnnouncementQueryDTO query) {
        return ApiResult.ok(announcementService.pageAnnouncements(query));
    }

    /**
     * 创建公告
     *
     * @param dto 创建请求
     * @return 操作结果
     */
    @OperationLog(module = "平台管理", operation = "创建公告")
    @PostMapping
    public ApiResult<Void> create(@Validated @RequestBody AnnouncementCreateDTO dto) {
        announcementService.createAnnouncement(dto);
        return ApiResult.ok();
    }

    /**
     * 更新公告
     *
     * @param id  公告ID
     * @param dto 更新请求
     * @return 操作结果
     */
    @OperationLog(module = "平台管理", operation = "更新公告")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @Validated @RequestBody AnnouncementCreateDTO dto) {
        announcementService.updateAnnouncement(id, dto);
        return ApiResult.ok();
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @OperationLog(module = "平台管理", operation = "发布公告")
    @PostMapping("/{id}/publish")
    public ApiResult<Void> publish(@PathVariable("id") Long id) {
        announcementService.publishAnnouncement(id);
        return ApiResult.ok();
    }

    /**
     * 下线公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @OperationLog(module = "平台管理", operation = "下线公告")
    @PutMapping("/{id}/offline")
    public ApiResult<Void> offline(@PathVariable("id") Long id) {
        announcementService.offlineAnnouncement(id);
        return ApiResult.ok();
    }
}
