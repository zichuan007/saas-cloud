package com.saas.cloud.workflow.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.workflow.api.dto.NodeConfigDTO;
import com.saas.cloud.workflow.api.vo.NodeConfigVO;
import com.saas.cloud.workflow.entity.WfNodeConfig;

/**
 * 流程节点审批人配置表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWfNodeConfigService extends IService<WfNodeConfig> {

    /**
     * 保存节点配置（先删除已有配置，再批量插入）
     *
     * @param processDefinitionId Flowable流程定义ID
     * @param configs             节点配置列表
     */
    void saveNodeConfigs(String processDefinitionId, List<NodeConfigDTO> configs);

    /**
     * 查询节点配置列表
     *
     * @param processDefinitionId Flowable流程定义ID
     * @return 节点配置视图列表
     */
    List<NodeConfigVO> getNodeConfigs(String processDefinitionId);
}
