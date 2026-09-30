package com.saas.cloud.workflow.domain.strategy.impl;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.rbac.api.feign.RbacFeignClient;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.workflow.api.dto.AssigneeDTO;
import com.saas.cloud.workflow.api.enums.AssignTypeEnum;
import com.saas.cloud.workflow.domain.strategy.AssignmentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeptAssignmentStrategy implements AssignmentStrategy {

    private final RbacFeignClient rbacFeignClient;

    @Override
    public AssignTypeEnum getType() {
        return AssignTypeEnum.DEPT;
    }

    @Override
    public List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables) {
        try {
            Long deptId = Long.parseLong(assignValue);
            ApiResult<UserInfoVO> result = rbacFeignClient.getDeptLeader(deptId);
            if (result != null && result.getData() != null) {
                UserInfoVO leader = result.getData();
                return Collections.singletonList(
                        AssigneeDTO.builder()
                                .userId(String.valueOf(leader.getId()))
                                .userName(leader.getRealName())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("查询部门负责人失败: deptId={}", assignValue, e);
        }
        return Collections.emptyList();
    }
}