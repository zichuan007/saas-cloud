package com.saas.cloud.platform.service;

import com.saas.cloud.platform.entity.GlobalConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 全局配置服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IGlobalConfigService extends IService<GlobalConfig> {

    /**
     * 获取配置值
     *
     * @param configKey 配置键
     * @return 配置值，不存在则返回 null
     */
    String getConfigValue(String configKey);

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    void updateConfig(String configKey, String configValue);

    /**
     * 列出所有配置
     *
     * @return 配置列表
     */
    List<GlobalConfig> listConfigs();
}
