package com.saas.cloud.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.workflow.api.dto.ProcessQueryDTO;
import com.saas.cloud.workflow.api.dto.ProcessStartDTO;
import com.saas.cloud.workflow.entity.WfProcessInstanceExt;

import java.util.List;
import java.util.Map;

/**
 * 流程实例扩展表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWfProcessInstanceExtService extends IService<WfProcessInstanceExt> {

    /**
     * 发起流程
     *
     * @param dto 发起请求
     * @return 流程实例扩展记录
     */
    WfProcessInstanceExt startProcess(ProcessStartDTO dto);

    /**
     * 分页查询我发起的流程
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<WfProcessInstanceExt> pageMyInitiated(ProcessQueryDTO query);

    /**
     * 获取流程详情（含审批时间线）
     *
     * @param id 扩展表主键ID
     * @return 流程详情信息
     */
    Map<String, Object> getProcessDetail(Long id);

    /**
     * 获取流程图高亮信息
     *
     * @param id 扩展表主键ID
     * @return 包含 bpmnXml 和高亮节点/连线信息
     */
    Map<String, Object> getProcessDiagram(Long id);

    /**
     * 撤回流程
     *
     * @param id 扩展表主键ID
     */
    void cancelProcess(Long id);

    /**
     * 分页查询运行中的流程实例（管理员监控）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<WfProcessInstanceExt> pageRunningInstances(ProcessQueryDTO query);

    /**
     * 强制终止流程（管理员操作）
     *
     * @param id     扩展表主键ID
     * @param reason 终止原因
     */
    void terminateProcess(Long id, String reason);
}
