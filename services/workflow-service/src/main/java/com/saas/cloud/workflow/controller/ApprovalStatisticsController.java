package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.api.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalStatisticsMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalStatisticsDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审批统计")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalStatisticsController {

    private final FlowApprovalStatisticsMapper statisticsMapper;

    @GetMapping("/overview")
    @Operation(summary = "审批概览统计")
    public ApiResult<Map<String, Object>> overview() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResult.success(Map.of());
        List<FlowApprovalStatisticsDO> list = statisticsMapper.selectByUserId(userId);
        int total = list.stream().mapToInt(FlowApprovalStatisticsDO::getTotalCount).sum();
        int approved = list.stream().mapToInt(FlowApprovalStatisticsDO::getApprovedCount).sum();
        int rejected = list.stream().mapToInt(FlowApprovalStatisticsDO::getRejectedCount).sum();
        long avgMs = list.isEmpty() ? 0
                : (long) list.stream().mapToLong(s -> s.getAvgDurationMs() != null ? s.getAvgDurationMs() : 0).average().orElse(0);
        return ApiResult.success(Map.of(
                "totalCount", total,
                "approvedCount", approved,
                "rejectedCount", rejected,
                "avgDurationMs", avgMs
        ));
    }

    @GetMapping("/trend")
    @Operation(summary = "审批趋势")
    public ApiResult<List<FlowApprovalStatisticsDO>> trend(
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.success(List.of())
                : ApiResult.success(statisticsMapper.selectByUserIdAndPeriod(userId, period));
    }
}