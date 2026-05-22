package com.saas.cloud.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.rbac.api.dto.DictDataCreateDTO;
import com.saas.cloud.rbac.api.vo.DictDataVO;
import com.saas.cloud.rbac.entity.DictData;
import com.saas.cloud.rbac.mapper.DictDataMapper;
import com.saas.cloud.rbac.service.IDictDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 字典数据 Service 实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DictDataServiceImpl implements IDictDataService {

    private final DictDataMapper dictDataMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DICT_CACHE_PREFIX = "dict:type:";

    @Override
    @SuppressWarnings("unchecked")
    public List<DictDataVO> listByDictType(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;
        List<DictDataVO> cached = (List<DictDataVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<DictData> dataList = dictDataMapper.selectList(
                new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getDictType, dictType)
                        .eq(DictData::getStatus, (byte) 1)
                        .orderByAsc(DictData::getSortOrder));

        List<DictDataVO> voList = dataList.stream().map(this::toVO).collect(Collectors.toList());
        redisTemplate.opsForValue().set(cacheKey, voList, 30, TimeUnit.MINUTES);
        return voList;
    }

    @Override
    public void createDictData(DictDataCreateDTO dto) {
        DictData entity = new DictData();
        entity.setDictType(dto.getDictType());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setStatus((byte) 1);
        entity.setCssClass(dto.getCssClass());
        entity.setListClass(dto.getListClass());
        entity.setRemark(dto.getRemark());
        dictDataMapper.insert(entity);

        redisTemplate.delete(DICT_CACHE_PREFIX + dto.getDictType());
    }

    @Override
    public void updateDictData(Long id, DictDataCreateDTO dto) {
        DictData entity = dictDataMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("字典数据不存在");
        }

        String oldDictType = entity.getDictType();
        entity.setDictType(dto.getDictType());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setCssClass(dto.getCssClass());
        entity.setListClass(dto.getListClass());
        entity.setRemark(dto.getRemark());
        dictDataMapper.updateById(entity);

        redisTemplate.delete(DICT_CACHE_PREFIX + oldDictType);
        if (!oldDictType.equals(dto.getDictType())) {
            redisTemplate.delete(DICT_CACHE_PREFIX + dto.getDictType());
        }
    }

    @Override
    public void deleteDictData(Long id) {
        DictData entity = dictDataMapper.selectById(id);
        if (entity == null) {
            return;
        }
        dictDataMapper.deleteById(id);
        redisTemplate.delete(DICT_CACHE_PREFIX + entity.getDictType());
    }

    private DictDataVO toVO(DictData entity) {
        DictDataVO vo = new DictDataVO();
        vo.setId(entity.getId());
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus() != null ? entity.getStatus().intValue() : null);
        vo.setCssClass(entity.getCssClass());
        vo.setListClass(entity.getListClass());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
