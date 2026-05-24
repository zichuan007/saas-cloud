package com.saas.cloud.common.excel.dict;

import java.util.Map;

/**
 * 字典数据提供者接口
 * <p>由业务侧实现，提供字典值到标签的映射关系。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface DictDataProvider {

    /**
     * 获取指定字典类型的值-标签映射
     *
     * @param dictType 字典类型编码
     * @return 字典值(String) → 字典标签 的映射
     */
    Map<String, String> getDictDataMap(String dictType);
}
