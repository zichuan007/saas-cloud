package com.saas.cloud.workflow.app.job;

import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalRecordMapper;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalStatisticsMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalStatisticsDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalStatisticsAggregationJob {

    private final FlowApprovalRecordMapper approvalRecordMapper;
    private final FlowApprovalStatisticsMapper statisticsMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregate() {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<FlowApprovalRecordDO> records = approvalRecordMapper.selectByActionTimeRange(yesterday, today);
        Map<Long, Map<String, List<FlowApprovalRecordDO>>> grouped = records.stream()
                .collect(Collectors.groupingBy(FlowApprovalRecordDO::getOperatorId,
                        Collectors.groupingBy(r -> r.getProcessDefKey() != null ? r.getProcessDefKey() : "_ALL")));
        for (var userEntry : grouped.entrySet()) {
            for (var processEntry : userEntry.getValue().entrySet()) {
                List<FlowApprovalRecordDO> list = processEntry.getValue();
                FlowApprovalStatisticsDO stat = new FlowApprovalStatisticsDO();
                stat.setUserId(userEntry.getKey());
                stat.setProcessDefKey(processEntry.getKey());
                stat.setStatPeriod("MONTH");
                stat.setStatDate(LocalDate.now().minusDays(1));
                stat.setTotalCount(list.size());
                stat.setApprovedCount((int) list.stream().filter(r -> "APPROVED".equals(r.getAction())).count());
                stat.setRejectedCount((int) list.stream().filter(r -> "REJECTED".equals(r.getAction())).count());
                stat.setTransferredCount((int) list.stream().filter(r -> "TRANSFER".equals(r.getAction())).count());
                stat.setDelegatedCount((int) list.stream().filter(r -> "DELEGATE".equals(r.getAction())).count());
                statisticsMapper.insert(stat);
            }
        }
        log.info("审批统计聚合完成: {} 条记录, {} 个用户", records.size(), grouped.size());
    }
}