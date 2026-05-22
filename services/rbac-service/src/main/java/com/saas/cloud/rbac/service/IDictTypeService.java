package com.saas.cloud.rbac.service;

import java.util.List;

import com.saas.cloud.rbac.api.dto.DictTypeCreateDTO;
import com.saas.cloud.rbac.api.vo.DictTypeVO;

/**
 * 字典类型 Service 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
public interface IDictTypeService {

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    List<DictTypeVO> listDictTypes();

    /**
     * 创建字典类型
     *
     * @param dto 创建请求
     */
    void createDictType(DictTypeCreateDTO dto);

    /**
     * 更新字典类型
     *
     * @param id  字典类型ID
     * @param dto 更新请求
     */
    void updateDictType(Long id, DictTypeCreateDTO dto);

    /**
     * 删除字典类型（同时删除关联的字典数据）
     *
     * @param id 字典类型ID
     */
    void deleteDictType(Long id);

}
