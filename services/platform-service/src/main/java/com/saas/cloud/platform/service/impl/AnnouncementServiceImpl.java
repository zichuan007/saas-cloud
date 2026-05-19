package com.saas.cloud.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.platform.api.dto.AnnouncementCreateDTO;
import com.saas.cloud.platform.api.dto.AnnouncementQueryDTO;
import com.saas.cloud.platform.entity.Announcement;
import com.saas.cloud.platform.mapper.AnnouncementMapper;
import com.saas.cloud.platform.service.IAnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统公告表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class AnnouncementServiceImpl
        extends ServiceImpl<AnnouncementMapper, Announcement>
        implements IAnnouncementService {

    private static final byte STATUS_DRAFT = 0;
    private static final byte STATUS_PUBLISHED = 1;
    private static final byte STATUS_OFFLINE = 2;

    @Override
    public PageResult<Announcement> pageAnnouncements(AnnouncementQueryDTO query) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getTitle()), Announcement::getTitle, query.getTitle())
                .eq(query.getStatus() != null, Announcement::getStatus, query.getStatus())
                .eq(query.getType() != null, Announcement::getType, query.getType())
                .orderByDesc(Announcement::getCreateTime);

        Page<Announcement> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public void createAnnouncement(AnnouncementCreateDTO dto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setTargetType(dto.getTargetType() != null ? dto.getTargetType() : (byte) 0);
        announcement.setTargetTenantIds(dto.getTargetTenantIds());
        announcement.setExpireTime(dto.getExpireTime());
        announcement.setStatus(STATUS_DRAFT);
        save(announcement);
        log.info("创建公告成功, id={}, title={}", announcement.getId(), dto.getTitle());
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementCreateDTO dto) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException("已发布的公告不能编辑，请先下线");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setTargetType(dto.getTargetType() != null ? dto.getTargetType() : announcement.getTargetType());
        announcement.setTargetTenantIds(dto.getTargetTenantIds());
        announcement.setExpireTime(dto.getExpireTime());
        updateById(announcement);
        log.info("更新公告成功, id={}", id);
    }

    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException("公告已处于发布状态");
        }

        announcement.setStatus(STATUS_PUBLISHED);
        announcement.setPublishTime(LocalDateTime.now());
        updateById(announcement);
        log.info("发布公告成功, id={}, title={}", id, announcement.getTitle());
    }

    @Override
    public void offlineAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException("只有已发布的公告才能下线");
        }

        announcement.setStatus(STATUS_OFFLINE);
        updateById(announcement);
        log.info("下线公告成功, id={}, title={}", id, announcement.getTitle());
    }
}
