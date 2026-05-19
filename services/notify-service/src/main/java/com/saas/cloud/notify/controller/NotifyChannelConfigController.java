package com.saas.cloud.notify.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.notify.api.dto.ChannelConfigUpdateDTO;
import com.saas.cloud.notify.api.vo.ChannelConfigVO;
import com.saas.cloud.notify.service.INotifyChannelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 通知渠道配置 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
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
    @PutMapping("/{channelType}")
    public ApiResult<Void> update(@PathVariable("channelType") Byte channelType,
                                  @Valid @RequestBody ChannelConfigUpdateDTO dto) {
        channelConfigService.updateConfig(channelType, dto);
        return ApiResult.ok();
    }
}
