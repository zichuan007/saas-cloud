package com.saas.cloud.workflow.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.workflow.api.dto.TaskQueryDTO;
import com.saas.cloud.workflow.entity.WfCopy;

/**
 * 流程抄送表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWfCopyService extends IService<WfCopy> {

    /**
     * 分页查询抄送给我的记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<WfCopy> pageMyCopies(TaskQueryDTO query);

    /**
     * 标记抄送记录已读
     *
     * @param id 抄送记录主键
     */
    void markAsRead(Long id);

    /**
     * 批量创建抄送记录
     *
     * @param processInstanceId 流程实例ID
     * @param processName       流程名称
     * @param title             流程标题
     * @param initiatorId       发起人ID
     * @param initiatorName     发起人姓名
     * @param taskName          当前节点名称
     * @param receiverIds       接收人ID列表
     */
    void createCopies(String processInstanceId, String processName, String title,
                      Long initiatorId, String initiatorName, String taskName,
                      List<Long> receiverIds);
}
