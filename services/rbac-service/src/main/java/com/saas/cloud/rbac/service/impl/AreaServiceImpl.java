package com.saas.cloud.rbac.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.rbac.api.vo.AreaVO;
import com.saas.cloud.rbac.entity.Area;
import com.saas.cloud.rbac.mapper.AreaMapper;
import com.saas.cloud.rbac.service.IAreaService;

/**
 * 行政区划服务实现
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-05-23
 */
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements IAreaService {

    @Override
    public List<AreaVO> listByParentCode(String parentCode) {
        List<Area> areas = this.list(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentCode, parentCode)
                .orderByAsc(Area::getSortOrder));
        return areas.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<AreaVO> search(String keyword) {
        List<Area> areas = this.list(new LambdaQueryWrapper<Area>()
                .and(w -> w
                        .like(Area::getAreaName, keyword)
                        .or().like(Area::getPinyin, keyword)
                        .or().like(Area::getFirstLetter, keyword))
                .last("LIMIT 50"));
        return areas.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AreaVO getByCode(String areaCode) {
        Area area = this.getOne(new LambdaQueryWrapper<Area>()
                .eq(Area::getAreaCode, areaCode)
                .last("LIMIT 1"));
        return area == null ? null : toVO(area);
    }

    @Override
    public List<AreaVO> getPath(String areaCode) {
        List<AreaVO> path = new ArrayList<>();
        String code = areaCode;
        while (code != null && !"0".equals(code)) {
            Area area = this.getOne(new LambdaQueryWrapper<Area>()
                    .eq(Area::getAreaCode, code)
                    .last("LIMIT 1"));
            if (area == null) {
                break;
            }
            path.add(toVO(area));
            code = area.getParentCode();
        }
        Collections.reverse(path);
        return path;
    }

    private AreaVO toVO(Area area) {
        AreaVO vo = new AreaVO();
        vo.setId(area.getId());
        vo.setAreaCode(area.getAreaCode());
        vo.setParentCode(area.getParentCode());
        vo.setAreaName(area.getAreaName());
        vo.setShortName(area.getShortName());
        vo.setMergerName(area.getMergerName());
        vo.setPinyin(area.getPinyin());
        vo.setFirstLetter(area.getFirstLetter());
        vo.setAreaLevel(area.getAreaLevel());
        vo.setZipCode(area.getZipCode());
        vo.setCityCode(area.getCityCode());
        vo.setLng(area.getLng());
        vo.setLat(area.getLat());
        return vo;
    }
}
