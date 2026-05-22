package com.saas.cloud.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.entity.GlobalConfig;
import com.saas.cloud.platform.service.IGlobalConfigService;

import lombok.RequiredArgsConstructor;

/**
 * 全局配置管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GlobalConfigController {

    private final IGlobalConfigService globalConfigService;

    /**
     * 查询所有配置列表
     *
     * @return 配置列表
     */
    @GetMapping("/list")
    public ApiResult<List<GlobalConfig>> listConfigs() {
        return ApiResult.ok(globalConfigService.listConfigs());
    }

    /**
     * 更新配置值
     *
     * @param key   配置键
     * @param value 配置值
     * @return 操作结果
     */
    @PutMapping("/{key}")
    public ApiResult<Void> updateConfig(@PathVariable("key") String key,
                                        @RequestParam("value") String value) {
        globalConfigService.updateConfig(key, value);
        return ApiResult.ok();
    }
}
