package com.saas.cloud.workflow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.workflow.api.dto.ProcessQueryDTO;
import com.saas.cloud.workflow.api.dto.ProcessStartDTO;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;
import com.saas.cloud.workflow.entity.WfProcessInstanceExt;
import com.saas.cloud.workflow.service.IWfProcessDefinitionExtService;
import com.saas.cloud.workflow.service.IWfProcessInstanceExtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 流程实例控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "流程实例管理")
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfProcessInstanceExtController {

    private final IWfProcessInstanceExtService processInstanceExtService;
    private final IWfProcessDefinitionExtService processDefinitionExtService;

    /**
     * 查询可发起的流程定义列表
     *
     * @return 已激活的流程定义列表
     */
    @Operation(summary = "查询可发起的流程定义列表")
    @GetMapping("/startable-list")
    public ApiResult<List<WfProcessDefinitionExt>> startableList() {
        List<WfProcessDefinitionExt> list = processDefinitionExtService.list(
                new LambdaQueryWrapper<WfProcessDefinitionExt>()
                        .eq(WfProcessDefinitionExt::getStatus, (byte) 1)
                        .orderByAsc(WfProcessDefinitionExt::getSortOrder));
        return ApiResult.ok(list);
    }

    /**
     * 发起流程
     *
     * @param dto 发起请求
     * @return 流程实例信息
     */
    @Operation(summary = "发起流程")
    @OperationLog(module = "流程审批", operation = "发起流程")
    @PostMapping("/start")
    public ApiResult<WfProcessInstanceExt> start(@Validated @RequestBody ProcessStartDTO dto) {
        return ApiResult.ok(processInstanceExtService.startProcess(dto));
    }

    /**
     * 分页查询我发起的流程
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询我发起的流程")
    @GetMapping("/my-initiated")
    public ApiResult<PageResult<WfProcessInstanceExt>> myInitiated(ProcessQueryDTO query) {
        return ApiResult.ok(processInstanceExtService.pageMyInitiated(query));
    }

    /**
     * 获取流程详情（含审批时间线）
     *
     * @param id 扩展表主键ID
     * @return 流程详情
     */
    @Operation(summary = "获取流程详情")
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(processInstanceExtService.getProcessDetail(id));
    }

    /**
     * 获取流程图高亮信息
     *
     * @param id 扩展表主键ID
     * @return 高亮节点信息
     */
    @Operation(summary = "获取流程图高亮信息")
    @GetMapping("/{id}/diagram")
    public ApiResult<Map<String, Object>> diagram(@PathVariable("id") Long id) {
        return ApiResult.ok(processInstanceExtService.getProcessDiagram(id));
    }

    /**
     * 撤回流程（仅发起人可操作）
     *
     * @param id 扩展表主键ID
     * @return 操作结果
     */
    @Operation(summary = "撤回流程")
    @OperationLog(module = "流程审批", operation = "撤回流程")
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable("id") Long id) {
        processInstanceExtService.cancelProcess(id);
        return ApiResult.ok();
    }
}
