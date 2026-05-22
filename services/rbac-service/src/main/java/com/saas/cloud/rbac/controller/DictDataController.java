package com.saas.cloud.rbac.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.DictDataCreateDTO;
import com.saas.cloud.rbac.api.vo.DictDataVO;
import com.saas.cloud.rbac.service.IDictDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 字典数据管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DictDataController {

    private final IDictDataService dictDataService;

    /**
     * 根据字典类型查询数据列表
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    @GetMapping("/type/{dictType}")
    public ApiResult<List<DictDataVO>> listByType(@PathVariable("dictType") String dictType) {
        return ApiResult.ok(dictDataService.listByDictType(dictType));
    }

    /**
     * 创建字典数据
     *
     * @param dto 创建请求
     * @return 操作结果
     */
    @OperationLog(module = "字典管理", operation = "创建字典数据")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody DictDataCreateDTO dto) {
        dictDataService.createDictData(dto);
        return ApiResult.ok();
    }

    /**
     * 更新字典数据
     *
     * @param id  字典数据ID
     * @param dto 更新请求
     * @return 操作结果
     */
    @OperationLog(module = "字典管理", operation = "更新字典数据")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody DictDataCreateDTO dto) {
        dictDataService.updateDictData(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除字典数据
     *
     * @param id 字典数据ID
     * @return 操作结果
     */
    @OperationLog(module = "字典管理", operation = "删除字典数据")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        dictDataService.deleteDictData(id);
        return ApiResult.ok();
    }
}
