package com.saas.cloud.notify.job;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.notify.entity.NotifyMessage;
import com.saas.cloud.notify.service.INotifyMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 清理已读消息定时任务
 * <p>清理 30 天前的已读站内消息</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CleanReadMessageJobHandler {

    private static final int RETENTION_DAYS = 30;

    private final INotifyMessageService messageService;

    /**
     * 清理 30 天前已读消息
     * <p>建议 Cron: 0 0 4 ? * SUN (每周日凌晨 4:00)</p>
     */
    @XxlJob("cleanReadMessageJob")
    public void execute() {

        log.info("[XXL-Job] 开始清理 {} 天前已读消息", RETENTION_DAYS);

        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        // 定时任务线程无租户上下文，平台级清理需跨租户执行
        long count = TenantContext.executeWithoutTenant(() -> {
            LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NotifyMessage::getIsRead, (byte) 1)
                    .le(NotifyMessage::getReadTime, threshold);
            long c = messageService.count(wrapper);
            if (c > 0) {
                messageService.remove(wrapper);
            }
            return c;
        });

        String msg = "清理完成, 删除已读消息: " + count + " 条";
        log.info("[XXL-Job] {}", msg);
        XxlJobHelper.handleSuccess(msg);
    }

}
