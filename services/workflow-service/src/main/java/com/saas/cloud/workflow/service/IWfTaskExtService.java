package com.saas.cloud.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.workflow.api.dto.*;
import com.saas.cloud.workflow.entity.WfTaskExt;

/**
 * 任务扩展表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWfTaskExtService extends IService<WfTaskExt> {

    /**
     * 分页查询我的待办任务
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<WfTaskExt> pageTodoTasks(TaskQueryDTO query);

    /**
     * 分页查询我的已办任务
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<WfTaskExt> pageDoneTasks(TaskQueryDTO query);

    /**
     * 审批通过
     *
     * @param dto 审批通过请求
     */
    void approve(TaskApproveDTO dto);

    /**
     * 驳回
     *
     * @param dto 驳回请求
     */
    void reject(TaskRejectDTO dto);

    /**
     * 转办
     *
     * @param dto 转办请求
     */
    void transfer(TaskTransferDTO dto);

    /**
     * 委派
     *
     * @param dto 委派请求
     */
    void delegate(TaskDelegateDTO dto);

    /**
     * 加签
     *
     * @param dto 加签请求
     */
    void addSign(TaskAddSignDTO dto);

    /**
     * 催办
     *
     * @param taskId Flowable任务ID
     */
    void urge(String taskId);
}
