package com.saas.cloud.rbac.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.storage.service.MinioService;
import com.saas.cloud.rbac.api.vo.ExportTaskVO;
import com.saas.cloud.rbac.entity.ExportTask;
import com.saas.cloud.rbac.mapper.ExportTaskMapper;
import com.saas.cloud.rbac.service.IExportTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 导出任务 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask>
        implements IExportTaskService {

    private final MinioService minioService;

    private static final String EXPORT_BUCKET = "saas-export";
    private static final int EXPIRE_DAYS = 7;
    private static final int DOWNLOAD_URL_EXPIRY_MINUTES = 30;

    @Override
    public Long submitTask(String taskName, String fileName, Consumer<OutputStream> exportAction) {
        ExportTask task = new ExportTask();
        task.setTaskName(taskName);
        task.setTaskType("export");
        task.setStatus(0);
        task.setFileName(fileName + ".xlsx");
        task.setExpireTime(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        task.setDownloadCount(0);
        baseMapper.insert(task);
        log.info("提交导出任务, id={}, name={}", task.getId(), taskName);

        executeExportAsync(task.getId(), fileName, exportAction);
        return task.getId();
    }

    @Async
    protected void executeExportAsync(Long taskId, String fileName, Consumer<OutputStream> exportAction) {
        ExportTask task = baseMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        try {
            task.setStatus(1);
            baseMapper.updateById(task);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exportAction.accept(bos);
            byte[] data = bos.toByteArray();

            String objectName = "export/" + taskId + "/" + fileName + ".xlsx";
            minioService.upload(EXPORT_BUCKET, objectName,
                    new ByteArrayInputStream(data),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            task.setStatus(2);
            task.setFilePath(objectName);
            task.setFileSize((long) data.length);
            baseMapper.updateById(task);
            log.info("导出任务完成, id={}, size={}", taskId, data.length);
        } catch (Exception e) {
            log.error("导出任务失败, id={}", taskId, e);
            task.setStatus(3);
            task.setErrorMsg(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "未知错误");
            baseMapper.updateById(task);
        }
    }

    @Override
    public PageResult<ExportTaskVO> queryMyTasks(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExportTask::getCreateTime);
        Page<ExportTask> page = new Page<>(pageNum, pageSize);
        Page<ExportTask> result = baseMapper.selectPage(page, wrapper);

        List<ExportTaskVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public String getDownloadUrl(Long taskId) {
        ExportTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("导出任务不存在");
        }
        if (task.getStatus() != 2) {
            throw new BusinessException("导出任务尚未完成");
        }
        try {
            task.setDownloadCount(task.getDownloadCount() + 1);
            baseMapper.updateById(task);
            return minioService.getPresignedUrl(EXPORT_BUCKET, task.getFilePath(), DOWNLOAD_URL_EXPIRY_MINUTES);
        } catch (Exception e) {
            log.error("获取下载链接失败, taskId={}", taskId, e);
            throw new BusinessException("获取下载链接失败");
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        ExportTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("导出任务不存在");
        }
        if (task.getFilePath() != null) {
            try {
                minioService.remove(EXPORT_BUCKET, task.getFilePath());
            } catch (Exception e) {
                log.warn("删除MinIO文件失败, path={}", task.getFilePath(), e);
            }
        }
        baseMapper.deleteById(taskId);
        log.info("删除导出任务, id={}", taskId);
    }

    @Override
    public int cleanExpiredTasks() {
        LambdaQueryWrapper<ExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(ExportTask::getExpireTime, LocalDateTime.now());
        List<ExportTask> expiredTasks = baseMapper.selectList(wrapper);

        for (ExportTask task : expiredTasks) {
            if (task.getFilePath() != null) {
                try {
                    minioService.remove(EXPORT_BUCKET, task.getFilePath());
                } catch (Exception e) {
                    log.warn("清理过期任务MinIO文件失败, id={}, path={}", task.getId(), task.getFilePath());
                }
            }
            baseMapper.deleteById(task.getId());
        }
        log.info("清理过期导出任务, 共{}条", expiredTasks.size());
        return expiredTasks.size();
    }

    private ExportTaskVO toVO(ExportTask entity) {
        ExportTaskVO vo = new ExportTaskVO();
        vo.setId(entity.getId());
        vo.setTaskName(entity.getTaskName());
        vo.setTaskType(entity.getTaskType());
        vo.setStatus(entity.getStatus());
        vo.setFileName(entity.getFileName());
        vo.setFileSize(entity.getFileSize());
        vo.setErrorMsg(entity.getErrorMsg());
        vo.setExpireTime(entity.getExpireTime());
        vo.setDownloadCount(entity.getDownloadCount());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
