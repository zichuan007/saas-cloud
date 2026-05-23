package com.saas.cloud.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.NoticeCreateDTO;
import com.saas.cloud.rbac.api.vo.NoticeVO;
import com.saas.cloud.rbac.entity.Notice;

/**
 * 通知公告服务
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface INoticeService extends IService<Notice> {

    /**
     * 分页查询公告（管理端）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    标题关键字
     * @return 公告分页
     */
    PageResult<NoticeVO> pageNotice(Integer pageNum, Integer pageSize, String title);

    /**
     * 分页查询已发布公告（用户端，含已读状态）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   当前用户ID
     * @return 公告分页
     */
    PageResult<NoticeVO> pagePublished(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 创建公告（草稿）
     *
     * @param dto 创建请求
     */
    void create(NoticeCreateDTO dto);

    /**
     * 更新公告
     *
     * @param id  公告ID
     * @param dto 更新内容
     */
    void update(Long id, NoticeCreateDTO dto);

    /**
     * 发布公告
     *
     * @param id 公告ID
     */
    void publish(Long id);

    /**
     * 撤回公告
     *
     * @param id 公告ID
     */
    void revoke(Long id);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void delete(Long id);

    /**
     * 标记公告为已读
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     */
    void markRead(Long noticeId, Long userId);

    /**
     * 查询未读公告数
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long countUnread(Long userId);
}
