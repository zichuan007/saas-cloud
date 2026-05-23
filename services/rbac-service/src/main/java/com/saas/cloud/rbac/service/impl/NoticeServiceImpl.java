package com.saas.cloud.rbac.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.NoticeCreateDTO;
import com.saas.cloud.rbac.api.vo.NoticeVO;
import com.saas.cloud.rbac.entity.Notice;
import com.saas.cloud.rbac.entity.NoticeRead;
import com.saas.cloud.rbac.mapper.NoticeMapper;
import com.saas.cloud.rbac.mapper.NoticeReadMapper;
import com.saas.cloud.rbac.service.INoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知公告服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    private final NoticeReadMapper noticeReadMapper;

    @Override
    public PageResult<NoticeVO> pageNotice(Integer pageNum, Integer pageSize, String title) {
        Page<Notice> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notice>()
                        .like(title != null && !title.isBlank(), Notice::getTitle, title)
                        .orderByDesc(Notice::getCreateTime));
        List<NoticeVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<NoticeVO> pagePublished(Integer pageNum, Integer pageSize, Long userId) {
        Page<Notice> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, (byte) 1)
                        .orderByDesc(Notice::getPublishTime));

        Set<Long> readIds = noticeReadMapper.selectList(
                        new LambdaQueryWrapper<NoticeRead>().eq(NoticeRead::getUserId, userId))
                .stream().map(NoticeRead::getNoticeId).collect(Collectors.toSet());

        List<NoticeVO> voList = page.getRecords().stream().map(n -> {
            NoticeVO vo = toVO(n);
            vo.setRead(readIds.contains(n.getId()));
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void create(NoticeCreateDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setStatus((byte) 0);
        notice.setRemark(dto.getRemark());
        this.save(notice);
    }

    @Override
    public void update(Long id, NoticeCreateDTO dto) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        if (notice.getStatus() == 1) {
            throw new BusinessException("已发布的公告不能编辑");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setRemark(dto.getRemark());
        this.updateById(notice);
    }

    @Override
    public void publish(Long id) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        notice.setStatus((byte) 1);
        notice.setPublishTime(LocalDateTime.now());
        this.updateById(notice);
    }

    @Override
    public void revoke(Long id) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        notice.setStatus((byte) 2);
        this.updateById(notice);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public void markRead(Long noticeId, Long userId) {
        Long count = noticeReadMapper.selectCount(
                new LambdaQueryWrapper<NoticeRead>()
                        .eq(NoticeRead::getNoticeId, noticeId)
                        .eq(NoticeRead::getUserId, userId));
        if (count > 0) {
            return;
        }
        NoticeRead read = new NoticeRead();
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        noticeReadMapper.insert(read);
    }

    @Override
    public Long countUnread(Long userId) {
        long totalPublished = this.count(new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, (byte) 1));
        long readCount = noticeReadMapper.selectCount(
                new LambdaQueryWrapper<NoticeRead>().eq(NoticeRead::getUserId, userId));
        return Math.max(0, totalPublished - readCount);
    }

    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setNoticeType(notice.getNoticeType());
        vo.setStatus(notice.getStatus());
        vo.setPublishTime(notice.getPublishTime());
        vo.setCreateUserName(notice.getCreateUserName());
        vo.setCreateTime(notice.getCreateTime());
        return vo;
    }
}
