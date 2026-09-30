package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.workflow.tunnel.mapper.FlowCcRecordMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowCcRecordDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "抄送记录")
@RestController
@RequestMapping("/cc-record")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CcRecordController {

    private final FlowCcRecordMapper ccRecordMapper;

    @GetMapping("/my")
    @Operation(summary = "我的抄送列表")
    public ApiResult<List<FlowCcRecordDO>> myCopies() {
        Long userId = UserContext.getUserId();
        return userId == null ? ApiResult.ok(List.of())
                : ApiResult.ok(ccRecordMapper.selectByReceiverId(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public ApiResult<Void> markAsRead(@PathVariable Long id) {
        FlowCcRecordDO record = ccRecordMapper.selectById(id);
        if (record != null) {
            record.setIsRead(1);
            ccRecordMapper.updateById(record);
        }
        return ApiResult.ok();
    }
}