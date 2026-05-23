package com.saas.cloud.rbac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.DictTypeCreateDTO;
import com.saas.cloud.rbac.api.vo.DictTypeVO;
import com.saas.cloud.rbac.service.IDictTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典类型管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Tag(name = "字典类型管理")
@RestController
@RequestMapping("/dict/type")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DictTypeController {

    private final IDictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    @Operation(summary = "查询字典类型列表")
    @GetMapping("/list")
    public ApiResult<List<DictTypeVO>> list() {
        return ApiResult.ok(dictTypeService.listDictTypes());
    }

    /**
     * 创建字典类型
     *
     * @param dto 创建请求
     * @return 操作结果
     */
    @Operation(summary = "创建字典类型")
    @OperationLog(module = "字典管理", operation = "创建字典类型")
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody DictTypeCreateDTO dto) {
        dictTypeService.createDictType(dto);
        return ApiResult.ok();
    }

    /**
     * 更新字典类型
     *
     * @param id  字典类型ID
     * @param dto 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新字典类型")
    @OperationLog(module = "字典管理", operation = "更新字典类型")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody DictTypeCreateDTO dto) {
        dictTypeService.updateDictType(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除字典类型
     *
     * @param id 字典类型ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型")
    @OperationLog(module = "字典管理", operation = "删除字典类型")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        dictTypeService.deleteDictType(id);
        return ApiResult.ok();
    }
}
