package com.saas.cloud.platform.service.impl;

import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.platform.entity.GlobalConfig;
import com.saas.cloud.platform.mapper.GlobalConfigMapper;
import com.saas.cloud.platform.service.IGlobalConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 全局配置服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class GlobalConfigServiceImpl extends ServiceImpl<GlobalConfigMapper, GlobalConfig> implements IGlobalConfigService {

    @Override
    public String getConfigValue(String configKey) {
        GlobalConfig config = this.lambdaQuery()
                .eq(GlobalConfig::getConfigKey, configKey)
                .one();
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue) {
        GlobalConfig config = this.lambdaQuery()
                .eq(GlobalConfig::getConfigKey, configKey)
                .one();
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配置项不存在, configKey=" + configKey);
        }
        config.setConfigValue(configValue);
        this.updateById(config);
        log.info("更新全局配置成功, configKey={}, configValue={}", configKey, configValue);
    }

    @Override
    public List<GlobalConfig> listConfigs() {
        return this.lambdaQuery()
                .orderByAsc(GlobalConfig::getConfigKey)
                .list();
    }
}
