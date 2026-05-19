package com.saas.cloud.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionCreateDTO;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionQueryDTO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionDetailVO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionVO;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;

import java.util.List;

/**
 * 流程定义扩展表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWfProcessDefinitionExtService extends IService<WfProcessDefinitionExt> {

    /**
     * 分页查询流程定义列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ProcessDefinitionVO> pageDefinitions(ProcessDefinitionQueryDTO query);

    /**
     * 获取流程定义详情（包含节点配置）
     *
     * @param id 主键ID
     * @return 详情视图对象
     */
    ProcessDefinitionDetailVO getDefinitionDetail(Long id);

    /**
     * 创建流程定义
     *
     * @param dto 创建请求
     */
    void createDefinition(ProcessDefinitionCreateDTO dto);

    /**
     * 更新流程定义
     *
     * @param id  主键ID
     * @param dto 更新请求
     */
    void updateDefinition(Long id, ProcessDefinitionCreateDTO dto);

    /**
     * 删除流程定义
     *
     * @param id 主键ID
     */
    void deleteDefinition(Long id);

    /**
     * 部署流程定义（将 BPMN XML 部署到 Flowable 引擎）
     *
     * @param id      主键ID
     * @param bpmnXml BPMN XML 内容
     */
    void deployDefinition(Long id, String bpmnXml);

    /**
     * 更新流程定义状态（挂起/激活）
     *
     * @param id     主键ID
     * @param status 状态 0-挂起 1-激活
     */
    void updateStatus(Long id, Byte status);

    /**
     * 获取流程定义的 BPMN XML
     *
     * @param id 主键ID
     * @return BPMN XML 字符串
     */
    String getBpmnXml(Long id);

    /**
     * 查询平台模板列表（isTemplate=1，不按租户过滤）
     *
     * @return 模板列表
     */
    List<ProcessDefinitionVO> listTemplates();

    /**
     * 导入平台模板到当前租户
     *
     * @param templateId 模板ID
     */
    void importTemplate(Long templateId);

    /**
     * 保存 BPMN XML 草稿（不部署到 Flowable）
     *
     * @param id      主键ID
     * @param bpmnXml BPMN XML 内容
     */
    void saveBpmnDraft(Long id, String bpmnXml);
}
