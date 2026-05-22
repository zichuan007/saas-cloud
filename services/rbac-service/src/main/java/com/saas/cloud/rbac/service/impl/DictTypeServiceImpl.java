package com.saas.cloud.rbac.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.rbac.api.dto.DictTypeCreateDTO;
import com.saas.cloud.rbac.api.vo.DictTypeVO;
import com.saas.cloud.rbac.entity.DictData;
import com.saas.cloud.rbac.entity.DictType;
import com.saas.cloud.rbac.mapper.DictDataMapper;
import com.saas.cloud.rbac.mapper.DictTypeMapper;
import com.saas.cloud.rbac.service.IDictTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典类型 Service 实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DictTypeServiceImpl implements IDictTypeService {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DICT_CACHE_PREFIX = "dict:type:";

    @Override
    public List<DictTypeVO> listDictTypes() {
        List<DictType> types = dictTypeMapper.selectList(
                new LambdaQueryWrapper<DictType>().orderByAsc(DictType::getId));
        return types.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void createDictType(DictTypeCreateDTO dto) {
        Long count = dictTypeMapper.selectCount(
                new LambdaQueryWrapper<DictType>().eq(DictType::getDictType, dto.getDictType()));
        if (count > 0) {
            throw new BusinessException("字典类型编码已存在: " + dto.getDictType());
        }

        DictType entity = new DictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setStatus((byte) 1);
        entity.setRemark(dto.getRemark());
        dictTypeMapper.insert(entity);
    }

    @Override
    public void updateDictType(Long id, DictTypeCreateDTO dto) {
        DictType entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("字典类型不存在");
        }

        String oldDictType = entity.getDictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setRemark(dto.getRemark());
        dictTypeMapper.updateById(entity);

        // 如果编码变了，同步更新字典数据中的 dict_type
        if (!oldDictType.equals(dto.getDictType())) {
            List<DictData> dataList = dictDataMapper.selectList(
                    new LambdaQueryWrapper<DictData>().eq(DictData::getDictType, oldDictType));
            for (DictData data : dataList) {
                data.setDictType(dto.getDictType());
                dictDataMapper.updateById(data);
            }
        }

        redisTemplate.delete(DICT_CACHE_PREFIX + oldDictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        DictType entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            return;
        }

        dictTypeMapper.deleteById(id);
        dictDataMapper.delete(
                new LambdaQueryWrapper<DictData>().eq(DictData::getDictType, entity.getDictType()));
        redisTemplate.delete(DICT_CACHE_PREFIX + entity.getDictType());
    }

    private DictTypeVO toVO(DictType entity) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setStatus(entity.getStatus() != null ? entity.getStatus().intValue() : null);
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
