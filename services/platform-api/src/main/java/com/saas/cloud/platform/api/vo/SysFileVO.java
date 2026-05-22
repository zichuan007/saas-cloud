package com.saas.cloud.platform.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件管理视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
public class SysFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件MIME类型 */
    private String fileType;

    /** 文件后缀 */
    private String fileSuffix;

    /** 业务类型 */
    private String bizType;

    /** 关联业务ID */
    private String bizId;

    /** 预签名访问URL */
    private String url;

    /** 上传时间 */
    private LocalDateTime createTime;

    /** 上传人 */
    private String createUserName;
}
