package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 文件管理表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("sys_file")
public class SysFile extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    /** 存储路径（MinIO objectName） */
    @TableField("file_path")
    private String filePath;

    /** 文件大小（字节） */
    @TableField("file_size")
    private Long fileSize;

    /** 文件MIME类型 */
    @TableField("file_type")
    private String fileType;

    /** 文件后缀 */
    @TableField("file_suffix")
    private String fileSuffix;

    /** 存储桶名称 */
    @TableField("bucket_name")
    private String bucketName;

    /** 业务类型（avatar/document/attachment等） */
    @TableField("biz_type")
    private String bizType;

    /** 关联业务ID */
    @TableField("biz_id")
    private String bizId;
}
