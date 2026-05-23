package com.saas.cloud.rbac.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 导出任务表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_export_task")
public class ExportTask extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务名称 */
    @TableField("task_name")
    private String taskName;

    /** 任务类型 export-导出 template-模板 */
    @TableField("task_type")
    private String taskType;

    /** 状态 0-排队中 1-处理中 2-成功 3-失败 */
    @TableField("status")
    private Integer status;

    /** 文件名 */
    @TableField("file_name")
    private String fileName;

    /** MinIO objectName */
    @TableField("file_path")
    private String filePath;

    /** 文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 失败原因 */
    @TableField("error_msg")
    private String errorMsg;

    /** 过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 下载次数 */
    @TableField("download_count")
    private Integer downloadCount;

    /** 请求参数JSON */
    @TableField("request_params")
    private String requestParams;
}
