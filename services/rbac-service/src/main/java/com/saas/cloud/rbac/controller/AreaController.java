package com.saas.cloud.rbac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.rbac.api.vo.AreaVO;
import com.saas.cloud.rbac.service.IAreaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 行政区划
 *
 * @author saas-cloud
 * @version V2.0
 * @since 2026-05-23
 */
@Tag(name = "行政区划")
@RestController
@RequestMapping("/area")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AreaController {

    private final IAreaService areaService;

    /**
     * 查询子级区域（懒加载）
     *
     * @param parentCode 父级区划代码，"0" 查省级
     * @return 子级区域列表
     */
    @Operation(summary = "查询子级区域")
    @GetMapping("/children/{parentCode}")
    public ApiResult<List<AreaVO>> children(@PathVariable String parentCode) {
        return ApiResult.ok(areaService.listByParentCode(parentCode));
    }

    /**
     * 搜索区域（支持名称/拼音/首字母）
     *
     * @param keyword 关键字
     * @return 匹配的区域列表
     */
    @Operation(summary = "搜索区域")
    @GetMapping("/search")
    public ApiResult<List<AreaVO>> search(@RequestParam String keyword) {
        return ApiResult.ok(areaService.search(keyword));
    }

    /**
     * 根据区划代码查询区域详情
     *
     * @param areaCode 区划代码
     * @return 区域信息
     */
    @Operation(summary = "根据区划代码查询区域详情")
    @GetMapping("/code/{areaCode}")
    public ApiResult<AreaVO> getByCode(@PathVariable String areaCode) {
        return ApiResult.ok(areaService.getByCode(areaCode));
    }

    /**
     * 根据区划代码查询完整路径链（省 → 市 → 区）
     *
     * @param areaCode 区划代码
     * @return 从省级到当前节点的有序列表
     */
    @Operation(summary = "查询区域完整路径")
    @GetMapping("/path/{areaCode}")
    public ApiResult<List<AreaVO>> getPath(@PathVariable String areaCode) {
        return ApiResult.ok(areaService.getPath(areaCode));
    }
}
