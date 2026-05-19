package com.saas.cloud.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.platform.api.dto.AnnouncementCreateDTO;
import com.saas.cloud.platform.api.dto.AnnouncementQueryDTO;
import com.saas.cloud.platform.entity.Announcement;

/**
 * 系统公告表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IAnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<Announcement> pageAnnouncements(AnnouncementQueryDTO query);

    /**
     * 创建公告（草稿状态）
     *
     * @param dto 创建请求
     */
    void createAnnouncement(AnnouncementCreateDTO dto);

    /**
     * 更新公告
     *
     * @param id  公告ID
     * @param dto 更新请求
     */
    void updateAnnouncement(Long id, AnnouncementCreateDTO dto);

    /**
     * 发布公告
     *
     * @param id 公告ID
     */
    void publishAnnouncement(Long id);

    /**
     * 下线公告
     *
     * @param id 公告ID
     */
    void offlineAnnouncement(Long id);
}
