package com.saas.cloud.rbac.service;

import java.util.List;

import com.saas.cloud.rbac.api.dto.DictDataCreateDTO;
import com.saas.cloud.rbac.api.vo.DictDataVO;

/**
 * 字典数据 Service 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
public interface IDictDataService {

    /**
     * 根据字典类型编码查询字典数据列表
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataVO> listByDictType(String dictType);

    /**
     * 创建字典数据
     *
     * @param dto 创建请求
     */
    void createDictData(DictDataCreateDTO dto);

    /**
     * 更新字典数据
     *
     * @param id  字典数据ID
     * @param dto 更新请求
     */
    void updateDictData(Long id, DictDataCreateDTO dto);

    /**
     * 删除字典数据
     *
     * @param id 字典数据ID
     */
    void deleteDictData(Long id);
}
