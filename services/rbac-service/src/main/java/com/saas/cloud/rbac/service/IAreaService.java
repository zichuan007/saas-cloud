package com.saas.cloud.rbac.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.rbac.api.vo.AreaVO;
import com.saas.cloud.rbac.entity.Area;

/**
 * 行政区划服务
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-05-23
 */
public interface IAreaService extends IService<Area> {

    /**
     * 根据父级区划代码查询子级列表
     *
     * @param parentCode 父级区划代码（"0" 查省级）
     * @return 子级区域列表
     */
    List<AreaVO> listByParentCode(String parentCode);

    /**
     * 根据关键字搜索区域（支持名称/拼音/首字母）
     *
     * @param keyword 关键字
     * @return 匹配的区域列表
     */
    List<AreaVO> search(String keyword);

    /**
     * 根据区划代码查询区域详情
     *
     * @param areaCode 区划代码
     * @return 区域信息
     */
    AreaVO getByCode(String areaCode);

    /**
     * 根据区划代码查询完整路径链（省 → 市 → 区）
     *
     * @param areaCode 区划代码
     * @return 从省级到当前节点的有序列表
     */
    List<AreaVO> getPath(String areaCode);
}
