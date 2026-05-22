package com.saas.cloud.notify.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知渠道配置更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ChannelConfigUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否启用 0-否 1-是 */
    @NotNull(message = "启用状态不能为空")
    private Byte enabled;

    /** 渠道配置(JSON) */
    private String configJson;
}
