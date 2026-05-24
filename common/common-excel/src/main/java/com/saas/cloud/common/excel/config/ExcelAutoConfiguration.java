package com.saas.cloud.common.excel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import com.saas.cloud.common.excel.dict.DictDataProvider;
import com.saas.cloud.common.excel.dict.DictFormatConverter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 自动配置
 * <p>当容器中存在 {@link DictDataProvider} 实现时，自动初始化字典转换器。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@Configuration
@ConditionalOnBean(DictDataProvider.class)
public class ExcelAutoConfiguration {

    private final DictDataProvider dictDataProvider;

    public ExcelAutoConfiguration(DictDataProvider dictDataProvider) {
        this.dictDataProvider = dictDataProvider;
    }

    @PostConstruct
    public void init() {
        DictFormatConverter.init(dictDataProvider);
        log.info("[Excel] 字典转换器已初始化");
    }
}
