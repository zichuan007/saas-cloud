package com.saas.cloud.workflow.api.feign;

import com.saas.cloud.common.core.result.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 流程实例 Feign 客户端
 */
@FeignClient(value = "workflow-service", contextId = "processInstanceFeignClient", path = "/process")
public interface ProcessInstanceFeignClient {

    @GetMapping("/{id}")
    ApiResult<Map<String, Object>> getProcessDetail(@PathVariable Long id);

    @PostMapping("/{id}/cancel")
    ApiResult<Void> cancel(@PathVariable Long id, @RequestParam String reason);

    @PostMapping("/monitor/{id}/terminate")
    ApiResult<Void> terminate(@PathVariable Long id, @RequestParam String reason);
}