package com.saas.cloud.workflow.service.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionCreateDTO;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionQueryDTO;
import com.saas.cloud.workflow.api.vo.NodeConfigVO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionDetailVO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionVO;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;
import com.saas.cloud.workflow.mapper.WfProcessDefinitionExtMapper;
import com.saas.cloud.workflow.service.IWfNodeConfigService;
import com.saas.cloud.workflow.service.IWfProcessDefinitionExtService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 流程定义扩展表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfProcessDefinitionExtServiceImpl
        extends ServiceImpl<WfProcessDefinitionExtMapper, WfProcessDefinitionExt>
        implements IWfProcessDefinitionExtService {

    private final RepositoryService repositoryService;
    private final IWfNodeConfigService nodeConfigService;
    private final PlatformFeignClient platformFeignClient;

    /** 状态：挂起 */
    private static final byte STATUS_SUSPENDED = 0;
    /** 状态：激活 */
    private static final byte STATUS_ACTIVE = 1;
    /** 模板标识：自定义 */
    private static final byte TEMPLATE_CUSTOM = 0;
    /** 模板标识：平台模板 */
    private static final byte TEMPLATE_PLATFORM = 1;

    @Override
    public PageResult<ProcessDefinitionVO> pageDefinitions(ProcessDefinitionQueryDTO query) {
        LambdaQueryWrapper<WfProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getProcessName()),
                        WfProcessDefinitionExt::getProcessName, query.getProcessName())
                .eq(StrUtil.isNotBlank(query.getCategory()),
                        WfProcessDefinitionExt::getCategory, query.getCategory())
                .eq(query.getStatus() != null,
                        WfProcessDefinitionExt::getStatus, query.getStatus())
                .eq(WfProcessDefinitionExt::getIsTemplate, TEMPLATE_CUSTOM)
                .orderByAsc(WfProcessDefinitionExt::getSortOrder)
                .orderByDesc(WfProcessDefinitionExt::getCreateTime);

        Page<WfProcessDefinitionExt> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, wrapper);

        List<ProcessDefinitionVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public ProcessDefinitionDetailVO getDefinitionDetail(Long id) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        ProcessDefinitionDetailVO detail = new ProcessDefinitionDetailVO();
        detail.setId(ext.getId());
        detail.setProcessKey(ext.getProcessKey());
        detail.setProcessName(ext.getProcessName());
        detail.setCategory(ext.getCategory());
        detail.setIcon(ext.getIcon());
        detail.setDescription(ext.getDescription());
        detail.setFormType(ext.getFormType());
        detail.setFormUrl(ext.getFormUrl());
        detail.setFormConfig(ext.getFormConfig());
        detail.setProcessDefinitionId(ext.getProcessDefinitionId());
        detail.setVersion(ext.getVersion());
        detail.setStatus(ext.getStatus());
        detail.setSortOrder(ext.getSortOrder());
        detail.setCreateTime(ext.getCreateTime());
        detail.setStatusDesc(getStatusDesc(ext.getStatus()));

        if (StrUtil.isNotBlank(ext.getProcessDefinitionId())) {
            List<NodeConfigVO> nodeConfigs = nodeConfigService.getNodeConfigs(ext.getProcessDefinitionId());
            detail.setNodeConfigs(nodeConfigs);
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDefinition(ProcessDefinitionCreateDTO dto) {
        checkProcessKeyUnique(dto.getProcessKey(), null);

        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                long currentCount = lambdaQuery().count();
                ApiResult<Boolean> quotaResult = platformFeignClient.checkQuota(
                        tenantId, "PROCESS", (int) currentCount);
                if (quotaResult.isSuccess() && Boolean.FALSE.equals(quotaResult.getData())) {
                    throw new BusinessException("流程定义数已达套餐上限，请升级套餐");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("流程配额校验异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
            }
        }

        WfProcessDefinitionExt ext = new WfProcessDefinitionExt();
        ext.setProcessKey(dto.getProcessKey());
        ext.setProcessName(dto.getProcessName());
        ext.setCategory(dto.getCategory());
        ext.setIcon(dto.getIcon());
        ext.setDescription(dto.getDescription());
        ext.setFormType(dto.getFormType());
        ext.setFormUrl(dto.getFormUrl());
        ext.setFormConfig(dto.getFormConfig());
        ext.setSortOrder(dto.getSortOrder());
        ext.setProcessDefinitionId("");
        ext.setVersion(1);
        ext.setStatus(STATUS_SUSPENDED);
        ext.setIsTemplate(TEMPLATE_CUSTOM);
        save(ext);

        // 同步创建 Flowable Model
        Model model = repositoryService.newModel();
        model.setKey(ext.getProcessKey());
        model.setName(ext.getProcessName());
        model.setCategory(ext.getCategory());
        model.setTenantId(tenantId != null ? String.valueOf(tenantId) : "");
        repositoryService.saveModel(model);

        ext.setModelId(model.getId());
        updateById(ext);

        log.info("创建流程定义成功, id={}, processKey={}, modelId={}", ext.getId(), ext.getProcessKey(), model.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefinition(Long id, ProcessDefinitionCreateDTO dto) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        if (!ext.getProcessKey().equals(dto.getProcessKey())) {
            checkProcessKeyUnique(dto.getProcessKey(), id);
        }

        ext.setProcessKey(dto.getProcessKey());
        ext.setProcessName(dto.getProcessName());
        ext.setCategory(dto.getCategory());
        ext.setIcon(dto.getIcon());
        ext.setDescription(dto.getDescription());
        ext.setFormType(dto.getFormType());
        ext.setFormUrl(dto.getFormUrl());
        ext.setFormConfig(dto.getFormConfig());
        ext.setSortOrder(dto.getSortOrder());
        updateById(ext);

        // 同步更新 Flowable Model 元数据
        if (StrUtil.isNotBlank(ext.getModelId())) {
            Model model = repositoryService.getModel(ext.getModelId());
            if (model != null) {
                model.setName(dto.getProcessName());
                model.setCategory(dto.getCategory());
                repositoryService.saveModel(model);
            }
        }

        log.info("更新流程定义成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefinition(Long id) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        // 删除 Flowable 部署
        if (StrUtil.isNotBlank(ext.getProcessDefinitionId())) {
            try {
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(ext.getProcessDefinitionId())
                        .singleResult();
                if (pd != null) {
                    repositoryService.deleteDeployment(pd.getDeploymentId(), true);
                    log.info("删除 Flowable 部署成功, deploymentId={}", pd.getDeploymentId());
                }
            } catch (Exception e) {
                log.warn("删除 Flowable 部署失败, processDefinitionId={}, error={}",
                        ext.getProcessDefinitionId(), e.getMessage());
            }
        }

        // 删除 Flowable Model
        if (StrUtil.isNotBlank(ext.getModelId())) {
            try {
                repositoryService.deleteModel(ext.getModelId());
                log.info("删除 Flowable Model 成功, modelId={}", ext.getModelId());
            } catch (Exception e) {
                log.warn("删除 Flowable Model 失败, modelId={}, error={}", ext.getModelId(), e.getMessage());
            }
        }

        removeById(id);
        log.info("删除流程定义成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deployDefinition(Long id, String bpmnXml) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        Long tenantId = TenantContext.getTenantId();
        String tenantIdStr = tenantId != null ? String.valueOf(tenantId) : "";

        Deployment deployment = repositoryService.createDeployment()
                .name(ext.getProcessName())
                .tenantId(tenantIdStr)
                .addString(ext.getProcessKey() + ".bpmn20.xml", bpmnXml)
                .deploy();

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        if (pd == null) {
            throw new BusinessException("流程部署失败，未能获取到流程定义");
        }

        // 同步保存到 Flowable Model
        saveToModel(ext, bpmnXml);

        ext.setProcessDefinitionId(pd.getId());
        ext.setVersion(pd.getVersion());
        ext.setStatus(STATUS_ACTIVE);
        updateById(ext);

        log.info("部署流程定义成功, id={}, processDefinitionId={}, version={}",
                id, pd.getId(), pd.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Byte status) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        if (StrUtil.isBlank(ext.getProcessDefinitionId())) {
            throw new BusinessException("流程尚未部署，无法切换状态");
        }

        if (STATUS_SUSPENDED == status) {
            repositoryService.suspendProcessDefinitionById(ext.getProcessDefinitionId(), false, null);
            log.info("挂起流程定义成功, processDefinitionId={}", ext.getProcessDefinitionId());
        } else if (STATUS_ACTIVE == status) {
            repositoryService.activateProcessDefinitionById(ext.getProcessDefinitionId(), false, null);
            log.info("激活流程定义成功, processDefinitionId={}", ext.getProcessDefinitionId());
        } else {
            throw new BusinessException("无效的状态值，仅支持 0(挂起) 或 1(激活)");
        }

        ext.setStatus(status);
        updateById(ext);
    }

    @Override
    public String getBpmnXml(Long id) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        // 1. 优先从 Flowable Model 读取（ACT_GE_BYTEARRAY）
        if (StrUtil.isNotBlank(ext.getModelId())) {
            byte[] source = repositoryService.getModelEditorSource(ext.getModelId());
            if (source != null && source.length > 0) {
                return new String(source, StandardCharsets.UTF_8);
            }
        }

        // 2. fallback：从 Flowable 已部署版本读取
        if (StrUtil.isNotBlank(ext.getProcessDefinitionId())) {
            try (InputStream inputStream = repositoryService.getProcessModel(ext.getProcessDefinitionId())) {
                return IoUtil.read(inputStream, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("获取 BPMN XML 失败, processDefinitionId={}", ext.getProcessDefinitionId(), e);
                throw new BusinessException("获取 BPMN XML 失败: " + e.getMessage());
            }
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBpmnDraft(Long id, String bpmnXml) {
        WfProcessDefinitionExt ext = getById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }

        // 存入 Flowable Model（ACT_RE_MODEL + ACT_GE_BYTEARRAY）
        saveToModel(ext, bpmnXml);

        log.info("保存 BPMN 设计成功, id={}, modelId={}", id, ext.getModelId());
    }

    @Override
    public List<ProcessDefinitionVO> listTemplates() {
        LambdaQueryWrapper<WfProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessDefinitionExt::getIsTemplate, TEMPLATE_PLATFORM)
                .orderByAsc(WfProcessDefinitionExt::getSortOrder)
                .orderByDesc(WfProcessDefinitionExt::getCreateTime);

        List<WfProcessDefinitionExt> list = list(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate(Long templateId) {
        WfProcessDefinitionExt template = getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        if (TEMPLATE_PLATFORM != template.getIsTemplate()) {
            throw new BusinessException("指定的记录不是平台模板");
        }

        Long tenantId = TenantContext.getTenantId();
        LambdaQueryWrapper<WfProcessDefinitionExt> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(WfProcessDefinitionExt::getProcessKey, template.getProcessKey())
                .eq(WfProcessDefinitionExt::getTenantId, tenantId)
                .eq(WfProcessDefinitionExt::getIsTemplate, TEMPLATE_CUSTOM);
        long count = count(existWrapper);
        if (count > 0) {
            throw new BusinessException("当前租户下已存在相同流程标识: " + template.getProcessKey());
        }

        WfProcessDefinitionExt newExt = new WfProcessDefinitionExt();
        BeanUtil.copyProperties(template, newExt, "id", "tenantId", "createTime", "updateTime",
                "createUserId", "createUserName", "updateUserId", "updateUserName",
                "deleteFlag", "dataVersion", "processDefinitionId", "modelId");
        newExt.setIsTemplate(TEMPLATE_CUSTOM);
        newExt.setVersion(1);
        newExt.setStatus(STATUS_SUSPENDED);
        newExt.setProcessDefinitionId("");
        save(newExt);

        // 如果模板已部署到 Flowable，拷贝 BPMN XML 并重新部署
        if (StrUtil.isNotBlank(template.getProcessDefinitionId())) {
            try (InputStream inputStream = repositoryService.getProcessModel(template.getProcessDefinitionId())) {
                String bpmnXml = IoUtil.read(inputStream, StandardCharsets.UTF_8);
                deployDefinition(newExt.getId(), bpmnXml);
                log.info("导入模板并部署成功, templateId={}, newId={}", templateId, newExt.getId());
            } catch (Exception e) {
                log.warn("导入模板时重新部署失败, templateId={}, error={}", templateId, e.getMessage());
            }
        }

        log.info("导入模板成功, templateId={}, newId={}", templateId, newExt.getId());
    }

    /**
     * 保存 BPMN XML 到 Flowable Model（ACT_RE_MODEL + ACT_GE_BYTEARRAY）。
     * 如果扩展记录还没有关联 Model，则自动创建。
     *
     * @param ext     扩展记录
     * @param bpmnXml BPMN XML 内容
     */
    private void saveToModel(WfProcessDefinitionExt ext, String bpmnXml) {
        Model model;
        if (StrUtil.isNotBlank(ext.getModelId())) {
            model = repositoryService.getModel(ext.getModelId());
            if (model == null) {
                model = createNewModel(ext);
            }
        } else {
            model = createNewModel(ext);
        }

        model.setName(ext.getProcessName());
        model.setCategory(ext.getCategory());
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes(StandardCharsets.UTF_8));

        if (!model.getId().equals(ext.getModelId())) {
            ext.setModelId(model.getId());
            updateById(ext);
        }
    }

    /**
     * 在 Flowable 中创建新 Model
     */
    private Model createNewModel(WfProcessDefinitionExt ext) {
        Long tenantId = TenantContext.getTenantId();
        Model model = repositoryService.newModel();
        model.setKey(ext.getProcessKey());
        model.setName(ext.getProcessName());
        model.setCategory(ext.getCategory());
        model.setTenantId(tenantId != null ? String.valueOf(tenantId) : "");
        repositoryService.saveModel(model);
        return model;
    }

    private void checkProcessKeyUnique(String processKey, Long excludeId) {
        LambdaQueryWrapper<WfProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessDefinitionExt::getProcessKey, processKey)
                .eq(WfProcessDefinitionExt::getIsTemplate, TEMPLATE_CUSTOM)
                .ne(excludeId != null, WfProcessDefinitionExt::getId, excludeId);
        long count = count(wrapper);
        if (count > 0) {
            throw new BusinessException("流程标识 [" + processKey + "] 在当前租户下已存在");
        }
    }

    private ProcessDefinitionVO convertToVO(WfProcessDefinitionExt ext) {
        ProcessDefinitionVO vo = new ProcessDefinitionVO();
        vo.setId(ext.getId());
        vo.setProcessKey(ext.getProcessKey());
        vo.setProcessName(ext.getProcessName());
        vo.setCategory(ext.getCategory());
        vo.setIcon(ext.getIcon());
        vo.setDescription(ext.getDescription());
        vo.setFormType(ext.getFormType());
        vo.setVersion(ext.getVersion());
        vo.setStatus(ext.getStatus());
        vo.setSortOrder(ext.getSortOrder());
        vo.setCreateTime(ext.getCreateTime());
        vo.setStatusDesc(getStatusDesc(ext.getStatus()));
        return vo;
    }

    private String getStatusDesc(Byte status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_SUSPENDED:
                return "挂起";
            case STATUS_ACTIVE:
                return "激活";
            default:
                return "未知";
        }
    }
}
