package com.saas.cloud.notify.controller;

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
import com.saas.cloud.notify.api.dto.TemplateCreateDTO;
import com.saas.cloud.notify.entity.NotifyTemplate;
import com.saas.cloud.notify.service.INotifyTemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知模板 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/template")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotifyTemplateController {

    private final INotifyTemplateService templateService;

    /**
     * 查询模板列表
     *
     * @return 模板列表
     */
    @GetMapping("/list")
    public ApiResult<List<NotifyTemplate>> list() {
        return ApiResult.ok(templateService.listTemplates());
    }

    /**
     * 创建模板
     *
     * @param dto 模板创建请求
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody TemplateCreateDTO dto) {
        templateService.createTemplate(dto);
        return ApiResult.ok();
    }

    /**
     * 更新模板
     *
     * @param id  模板ID
     * @param dto 模板更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody TemplateCreateDTO dto) {
        templateService.updateTemplate(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        templateService.deleteTemplate(id);
        return ApiResult.ok();
    }
}
