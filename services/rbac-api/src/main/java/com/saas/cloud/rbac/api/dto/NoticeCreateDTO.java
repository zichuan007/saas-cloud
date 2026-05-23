package com.saas.cloud.rbac.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知公告创建 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class NoticeCreateDTO {

    /** 公告标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 公告内容 */
    private String content;

    /** 类型 1-通知 2-公告 */
    @NotNull(message = "类型不能为空")
    private Byte noticeType;

    /** 备注 */
    private String remark;
}
