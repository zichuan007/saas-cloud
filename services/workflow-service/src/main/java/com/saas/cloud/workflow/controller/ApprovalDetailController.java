package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.api.ApiResult;
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

    private final FlowApprovalRecordDAO approvalRecordDAO;

    @GetMapping("/{processInstanceId}")
    @Operation(summary = "查询流程审批记录")
    public ApiResult<List<FlowApprovalRecordDO>> detail(@PathVariable String processInstanceId) {
        return ApiResult.success(approvalRecordMapper.selectByProcessInstanceId(processInstanceId));
    }
}