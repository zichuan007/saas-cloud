package com.saas.cloud.platform.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.platform.api.vo.SysFileVO;
import com.saas.cloud.platform.entity.SysFile;

/**
 * 文件管理服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
public interface ISysFileService extends IService<SysFile> {

    /**
     * 上传文件（租户隔离路径）
     *
     * @param file    上传的文件
     * @param bizType 业务类型
     * @param bizId   关联业务ID（可选）
     * @return 文件信息
     */
    SysFileVO upload(MultipartFile file, String bizType, String bizId);

    /**
     * 获取文件预签名访问URL
     *
     * @param fileId 文件ID
     * @return 预签名URL
     */
    String getPresignedUrl(Long fileId);

    /**
     * 根据业务类型和业务ID查询文件列表
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件列表
     */
    List<SysFileVO> listByBiz(String bizType, String bizId);

    /**
     * 删除文件（同时删除 MinIO 存储和数据库记录）
     *
     * @param fileId 文件ID
     */
    void deleteFile(Long fileId);
}
