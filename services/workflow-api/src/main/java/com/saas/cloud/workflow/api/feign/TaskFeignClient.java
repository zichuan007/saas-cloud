package com.saas.cloud.workflow.api.feign;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.workflow.api.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务 Feign 客户端
 */
@FeignClient(value = "workflow-service", contextId = "taskFeignClient", path = "/task")
public interface TaskFeignClient {

    @GetMapping("/todo")
    ApiResult<List<Map<String, Object>>> getTodoTasks();

    @GetMapping("/done")
    ApiResult<List<Map<String, Object>>> getDoneTasks();

    @PostMapping("/{id}/approve")
    ApiResult<Void> approve(@PathVariable String id, @Valid @RequestBody ApproveRequest request);

    @PostMapping("/{id}/reject")
    ApiResult<Void> reject(@PathVariable String id, @Valid @RequestBody RejectRequest request);

    @PostMapping("/{id}/urge")
    ApiResult<Void> urge(@PathVariable String id, @Valid @RequestBody UrgeRequest request);
}