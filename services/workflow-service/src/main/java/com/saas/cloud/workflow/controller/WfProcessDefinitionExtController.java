package com.saas.cloud.workflow.controller;

import cn.hutool.core.util.StrUtil;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionCreateDTO;
import com.saas.cloud.workflow.api.dto.ProcessDefinitionQueryDTO;
import com.saas.cloud.workflow.api.dto.ProcessDeployDTO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionDetailVO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionVO;
import com.saas.cloud.workflow.service.IWfProcessDefinitionExtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 流程定义管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/definition")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfProcessDefinitionExtController {

    private final IWfProcessDefinitionExtService processDefinitionExtService;

    /**
     * 分页查询流程定义列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResult<PageResult<ProcessDefinitionVO>> list(ProcessDefinitionQueryDTO query) {
        return ApiResult.ok(processDefinitionExtService.pageDefinitions(query));
    }

    /**
     * 获取流程定义详情（含节点配置）
     *
     * @param id 主键ID
     * @return 详情
     */
    @GetMapping("/{id}")
    public ApiResult<ProcessDefinitionDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(processDefinitionExtService.getDefinitionDetail(id));
    }

    /**
     * 创建流程定义
     *
     * @param dto 创建请求
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "创建流程定义")
    @PostMapping
    public ApiResult<Void> create(@Validated @RequestBody ProcessDefinitionCreateDTO dto) {
        processDefinitionExtService.createDefinition(dto);
        return ApiResult.ok();
    }

    /**
     * 更新流程定义
     *
     * @param id  主键ID
     * @param dto 更新请求
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "更新流程定义")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @Validated @RequestBody ProcessDefinitionCreateDTO dto) {
        processDefinitionExtService.updateDefinition(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除流程定义
     *
     * @param id 主键ID
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "删除流程定义")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        processDefinitionExtService.deleteDefinition(id);
        return ApiResult.ok();
    }

    /**
     * 部署流程定义（将 BPMN XML 部署到 Flowable 引擎）
     *
     * @param id  主键ID
     * @param dto 部署请求（包含 BPMN XML）
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "部署流程定义")
    @PostMapping("/{id}/deploy")
    public ApiResult<Void> deploy(@PathVariable("id") Long id,
                                  @Validated @RequestBody ProcessDeployDTO dto) {
        processDefinitionExtService.deployDefinition(id, dto.getBpmnXml());
        return ApiResult.ok();
    }

    /**
     * 挂起/激活流程定义
     *
     * @param id     主键ID
     * @param status 状态 0-挂起 1-激活
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "更新流程状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @RequestParam("status") Byte status) {
        processDefinitionExtService.updateStatus(id, status);
        return ApiResult.ok();
    }

    /**
     * 获取流程定义的 BPMN XML
     *
     * @param id 主键ID
     * @return BPMN XML 字符串
     */
    @GetMapping("/{id}/bpmn-xml")
    public ApiResult<String> getBpmnXml(@PathVariable("id") Long id) {
        return ApiResult.ok(processDefinitionExtService.getBpmnXml(id));
    }

    /**
     * 查询平台模板列表
     *
     * @return 模板列表
     */
    @GetMapping("/template/list")
    public ApiResult<List<ProcessDefinitionVO>> templateList() {
        return ApiResult.ok(processDefinitionExtService.listTemplates());
    }

    /**
     * 导入平台模板到当前租户
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "导入流程模板")
    @PostMapping("/template/{id}/import")
    public ApiResult<Void> importTemplate(@PathVariable("id") Long id) {
        processDefinitionExtService.importTemplate(id);
        return ApiResult.ok();
    }

    /**
     * 保存 BPMN XML 草稿（不部署到引擎）
     *
     * @param id  主键ID
     * @param dto 包含 BPMN XML 的请求体
     * @return 操作结果
     */
    @OperationLog(module = "流程管理", operation = "保存流程设计草稿")
    @PutMapping("/{id}/bpmn-xml")
    public ApiResult<Void> saveBpmnDraft(@PathVariable("id") Long id,
                                         @Validated @RequestBody ProcessDeployDTO dto) {
        processDefinitionExtService.saveBpmnDraft(id, dto.getBpmnXml());
        return ApiResult.ok();
    }

    /**
     * 下载 BPMN XML 文件
     *
     * @param id 主键ID
     * @return XML 文件流
     */
    @GetMapping("/{id}/download-xml")
    public ResponseEntity<byte[]> downloadXml(@PathVariable("id") Long id) {
        String xml = processDefinitionExtService.getBpmnXml(id);
        if (StrUtil.isBlank(xml)) {
            throw new BusinessException("该流程定义暂无 BPMN XML");
        }
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=process_" + id + ".bpmn20.xml")
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
