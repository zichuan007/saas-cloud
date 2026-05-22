package com.saas.cloud.notify.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知模板创建/更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TemplateCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板编码 */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /** 渠道 0-站内信 1-邮件 2-IM Webhook */
    @NotNull(message = "渠道类型不能为空")
    private Byte type;

    /** 标题模板 */
    @NotBlank(message = "标题模板不能为空")
    private String titleTemplate;

    /** 内容模板 */
    @NotBlank(message = "内容模板不能为空")
    private String contentTemplate;
}
