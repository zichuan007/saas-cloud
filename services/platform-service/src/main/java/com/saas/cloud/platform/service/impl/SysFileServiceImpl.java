package com.saas.cloud.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.storage.config.MinioConfig;
import com.saas.cloud.common.storage.service.MinioService;
import com.saas.cloud.platform.api.vo.SysFileVO;
import com.saas.cloud.platform.entity.SysFile;
import com.saas.cloud.platform.mapper.SysFileMapper;
import com.saas.cloud.platform.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件管理服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioService minioService;
    private final MinioConfig minioConfig;

    @Override
    public SysFileVO upload(MultipartFile file, String bizType, String bizId) {
        String originalFilename = file.getOriginalFilename();
        String suffix = extractSuffix(originalFilename);
        Long tenantId = TenantContext.getTenantId();

        // 租户隔离路径: tenant/{tenantId}/{bizType}/{yyyy/MM/dd}/{uuid}.{suffix}
        String objectName = buildObjectName(tenantId, bizType, suffix);
        String bucketName = minioConfig.getBucketName();

        try {
            minioService.upload(bucketName, objectName, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new BusinessException("文件上传失败");
        }

        SysFile sysFile = new SysFile();
        sysFile.setFileName(originalFilename);
        sysFile.setFilePath(objectName);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        sysFile.setFileSuffix(suffix);
        sysFile.setBucketName(bucketName);
        sysFile.setBizType(bizType);
        sysFile.setBizId(bizId);
        save(sysFile);

        return toVO(sysFile);
    }

    @Override
    public String getPresignedUrl(Long fileId) {
        SysFile sysFile = getById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        try {
            return minioService.getPresignedUrl(sysFile.getBucketName(), sysFile.getFilePath(), 30);
        } catch (Exception e) {
            log.error("获取文件预签名URL失败: {}", sysFile.getFilePath(), e);
            throw new BusinessException("获取文件访问地址失败");
        }
    }

    @Override
    public List<SysFileVO> listByBiz(String bizType, String bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getBizType, bizType)
                .eq(SysFile::getBizId, bizId)
                .orderByDesc(SysFile::getCreateTime);
        return list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void deleteFile(Long fileId) {
        SysFile sysFile = getById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        try {
            minioService.remove(sysFile.getBucketName(), sysFile.getFilePath());
        } catch (Exception e) {
            log.error("删除MinIO文件失败: {}", sysFile.getFilePath(), e);
        }
        removeById(fileId);
    }

    private String buildObjectName(Long tenantId, String bizType, String suffix) {
        String datePath = LocalDate.now().format(DATE_FORMAT);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String bizDir = (bizType != null && !bizType.isEmpty()) ? bizType : "default";
        return String.format("tenant/%d/%s/%s/%s%s", tenantId, bizDir, datePath, uuid, suffix);
    }

    private String extractSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private SysFileVO toVO(SysFile sysFile) {
        SysFileVO vo = new SysFileVO();
        vo.setId(sysFile.getId());
        vo.setFileName(sysFile.getFileName());
        vo.setFileSize(sysFile.getFileSize());
        vo.setFileType(sysFile.getFileType());
        vo.setFileSuffix(sysFile.getFileSuffix());
        vo.setBizType(sysFile.getBizType());
        vo.setBizId(sysFile.getBizId());
        vo.setCreateTime(sysFile.getCreateTime());
        vo.setCreateUserName(sysFile.getCreateUserName());
        return vo;
    }
}
