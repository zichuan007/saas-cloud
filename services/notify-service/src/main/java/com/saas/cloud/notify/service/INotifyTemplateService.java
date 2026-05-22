package com.saas.cloud.notify.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.notify.api.dto.TemplateCreateDTO;
import com.saas.cloud.notify.entity.NotifyTemplate;

/**
 * 通知模板表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface INotifyTemplateService extends IService<NotifyTemplate> {

    /**
     * 查询模板列表
     *
     * @return 模板列表
     */
    List<NotifyTemplate> listTemplates();

    /**
     * 创建模板
     *
     * @param dto 模板创建请求
     */
    void createTemplate(TemplateCreateDTO dto);

    /**
     * 更新模板
     *
     * @param id  模板ID
     * @param dto 模板更新请求
     */
    void updateTemplate(Long id, TemplateCreateDTO dto);

    /**
     * 删除模板
     *
     * @param id 模板ID
     */
    void deleteTemplate(Long id);
}
