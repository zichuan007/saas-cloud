package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 导出任务视图VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class ExportTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long id;

    /** 任务名称 */
    private String taskName;

    /** 任务类型 */
    private String taskType;

    /** 状态 0-排队中 1-处理中 2-成功 3-失败 */
    private Integer status;

    /** 文件名 */
    private String fileName;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 失败原因 */
    private String errorMsg;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 下载次数 */
    private Integer downloadCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
