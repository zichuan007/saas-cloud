package com.saas.cloud.rbac.tenant;

import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializer;
import com.saas.cloud.rbac.entity.Dept;
import com.saas.cloud.rbac.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 租户初始化 - 创建根部门
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeptInitializer implements TenantInitializer {

    private final DeptMapper deptMapper;

    @Override
    public String getCode() {
        return "DEPT";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void initialize(TenantInitContext context) {
        Dept rootDept = new Dept();
        rootDept.setDeptName("总公司");
        rootDept.setParentId(0L);
        rootDept.setAncestors("0");
        rootDept.setLeader(context.getContactPerson());
        rootDept.setPhone(context.getContactPhone());
        rootDept.setSortOrder(0);
        rootDept.setStatus((byte) 1);
        rootDept.setTenantId(context.getTenantId());
        deptMapper.insert(rootDept);

        context.put("rootDeptId", rootDept.getId());
        log.info("创建根部门成功, deptId={}", rootDept.getId());
    }

    @Override
    public void rollback(TenantInitContext context) {
        Long deptId = context.get("rootDeptId");
        if (deptId != null) {
            deptMapper.deleteById(deptId);
            log.info("回滚根部门, deptId={}", deptId);
        }
    }
}
