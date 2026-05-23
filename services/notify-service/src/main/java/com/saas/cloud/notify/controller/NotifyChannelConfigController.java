package com.saas.cloud.notify.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.notify.api.dto.ChannelConfigUpdateDTO;
import com.saas.cloud.notify.api.vo.ChannelConfigVO;
import com.saas.cloud.notify.service.INotifyChannelConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知渠道配置 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "通知渠道配置管理")
@Slf4j
@RestController
@RequestMapping("/channel")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotifyChannelConfigController {

    private final INotifyChannelConfigService channelConfigService;

    /**
     * 查询渠道配置列表
     *
     * @return 渠道配置列表
     */
    @Operation(summary = "查询渠道配置列表")
    @GetMapping("/list")
    public ApiResult<List<ChannelConfigVO>> list() {
        return ApiResult.ok(channelConfigService.listConfigs());
    }

    /**
     * 更新渠道配置
     *
     * @param channelType 渠道类型
     * @param dto         配置更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新渠道配置")
    @PutMapping("/{channelType}")
    public ApiResult<Void> update(@PathVariable("channelType") Byte channelType,
                                  @Valid @RequestBody ChannelConfigUpdateDTO dto) {
        channelConfigService.updateConfig(channelType, dto);
        return ApiResult.ok();
    }
}
