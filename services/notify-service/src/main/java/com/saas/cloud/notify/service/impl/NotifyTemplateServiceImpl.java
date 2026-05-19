package com.saas.cloud.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.notify.api.dto.TemplateCreateDTO;
import com.saas.cloud.notify.entity.NotifyTemplate;
import com.saas.cloud.notify.mapper.NotifyTemplateMapper;
import com.saas.cloud.notify.service.INotifyTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知模板表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class NotifyTemplateServiceImpl extends ServiceImpl<NotifyTemplateMapper, NotifyTemplate> implements INotifyTemplateService {

    @Override
    public List<NotifyTemplate> listTemplates() {
        LambdaQueryWrapper<NotifyTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(NotifyTemplate::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public void createTemplate(TemplateCreateDTO dto) {
        // 校验模板编码唯一性
        long count = this.lambdaQuery()
                .eq(NotifyTemplate::getTemplateCode, dto.getTemplateCode())
                .count();
        if (count > 0) {
            throw new BusinessException("模板编码已存在: " + dto.getTemplateCode());
        }

        NotifyTemplate template = new NotifyTemplate();
        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setType(dto.getType());
        template.setTitleTemplate(dto.getTitleTemplate());
        template.setContentTemplate(dto.getContentTemplate());
        template.setStatus((byte) 1);
        this.save(template);
        log.info("[通知中心] 创建模板成功, templateCode={}", dto.getTemplateCode());
    }

    @Override
    public void updateTemplate(Long id, TemplateCreateDTO dto) {
        NotifyTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 如果修改了模板编码，校验新编码的唯一性
        if (!template.getTemplateCode().equals(dto.getTemplateCode())) {
            long count = this.lambdaQuery()
                    .eq(NotifyTemplate::getTemplateCode, dto.getTemplateCode())
                    .count();
            if (count > 0) {
                throw new BusinessException("模板编码已存在: " + dto.getTemplateCode());
            }
        }

        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setType(dto.getType());
        template.setTitleTemplate(dto.getTitleTemplate());
        template.setContentTemplate(dto.getContentTemplate());
        this.updateById(template);
        log.info("[通知中心] 更新模板成功, id={}, templateCode={}", id, dto.getTemplateCode());
    }

    @Override
    public void deleteTemplate(Long id) {
        NotifyTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        this.removeById(id);
        log.info("[通知中心] 删除模板成功, id={}, templateCode={}", id, template.getTemplateCode());
    }
}
