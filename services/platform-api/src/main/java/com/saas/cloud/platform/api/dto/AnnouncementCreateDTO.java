package com.saas.cloud.platform.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告创建/更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class AnnouncementCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 公告标题 */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /** 公告内容 */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 类型 0-通知 1-维护 2-功能更新 */
    @NotNull(message = "公告类型不能为空")
    private Byte type;

    /** 目标 0-全部租户 1-指定租户 */
    private Byte targetType;

    /** 指定租户ID列表(JSON) */
    private String targetTenantIds;

    /** 过期时间 */
    private LocalDateTime expireTime;
}
