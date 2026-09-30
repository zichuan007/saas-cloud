package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalRecordMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批详情 Controller
 */
@Tag(name = "审批详情")
@RestController
@RequestMapping("/approval-detail")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalDetailController {

    private final FlowApprovalRecordMapper approvalRecordMapper;

    @GetMapping("/{processInstanceId}")
    @Operation(summary = "查询流程审批记录")
    public ApiResult<List<FlowApprovalRecordDO>> detail(@PathVariable String processInstanceId) {
        return ApiResult.ok(approvalRecordMapper.selectList(
                new LambdaQueryWrapper<FlowApprovalRecordDO>()
                        .eq(FlowApprovalRecordDO::getProcessInstanceId, processInstanceId)));
    }
}