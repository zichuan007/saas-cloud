package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.workflow.api.dto.NodeConfigDTO;
import com.saas.cloud.workflow.api.vo.NodeConfigVO;
import com.saas.cloud.workflow.service.IWfNodeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 流程节点审批人配置控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/node-config")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
public class WfNodeConfigController {

    private final IWfNodeConfigService nodeConfigService;

    /**
     * 保存节点配置（先删后增）
     *
     * @param processDefinitionId Flowable流程定义ID
     * @param configs             节点配置列表
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "保存节点配置")
    @PostMapping
    public ApiResult<Void> saveNodeConfigs(
            @RequestParam("processDefinitionId") String processDefinitionId,
            @RequestBody List<@Valid NodeConfigDTO> configs) {
        nodeConfigService.saveNodeConfigs(processDefinitionId, configs);
        return ApiResult.ok();
    }

    /**
     * 查询节点配置列表
     *
     * @param processDefinitionId Flowable流程定义ID
     * @return 节点配置列表
     */
    @GetMapping("/{processDefinitionId}")
    public ApiResult<List<NodeConfigVO>> getNodeConfigs(
            @PathVariable("processDefinitionId") String processDefinitionId) {
        return ApiResult.ok(nodeConfigService.getNodeConfigs(processDefinitionId));
    }
}
