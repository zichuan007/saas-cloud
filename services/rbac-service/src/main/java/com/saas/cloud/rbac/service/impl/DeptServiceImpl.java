package com.saas.cloud.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.rbac.api.dto.DeptCreateDTO;
import com.saas.cloud.rbac.api.dto.DeptUpdateDTO;
import com.saas.cloud.rbac.api.vo.DeptTreeVO;
import com.saas.cloud.rbac.entity.Dept;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.mapper.DeptMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.service.IDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements IDeptService {

    private final UserMapper userMapper;
    private final PlatformFeignClient platformFeignClient;

    /**
     * 根节点的 parentId
     */
    private static final long ROOT_PARENT_ID = 0L;

    /**
     * 顶级部门的祖先链
     */
    private static final String ROOT_ANCESTORS = "0";

    @Override
    public List<DeptTreeVO> buildDeptTree(Long tenantId) {
        log.info("构建部门树, tenantId={}", tenantId);
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(tenantId != null, Dept::getTenantId, tenantId)
                .orderByAsc(Dept::getSortOrder);
        List<Dept> deptList = this.list(queryWrapper);
        // 转换为 VO 并构建树
        List<DeptTreeVO> voList = deptList.stream()
                .map(this::convertToDeptTreeVO)
                .collect(Collectors.toList());
        return buildTree(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDept(DeptCreateDTO dto) {
        log.info("创建部门, deptName={}, parentId={}", dto.getDeptName(), dto.getParentId());

        // 配额校验（非核心逻辑，Feign 调用失败时降级放行）
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                long currentDeptCount = this.count(new LambdaQueryWrapper<Dept>()
                        .eq(Dept::getTenantId, tenantId));
                ApiResult<Boolean> quotaResult = platformFeignClient.checkQuota(
                        tenantId, "DEPT", (int) currentDeptCount);
                if (quotaResult.isSuccess() && Boolean.FALSE.equals(quotaResult.getData())) {
                    throw new BusinessException(ResultCode.QUOTA_EXCEEDED, "部门数已达套餐上限，请升级套餐");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("部门配额校验异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
            }
        }

        Dept dept = new Dept();
        dept.setDeptName(dto.getDeptName());
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : ROOT_PARENT_ID);
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        dept.setStatus((byte) 1);

        // 维护 ancestors 祖先链
        if (ROOT_PARENT_ID == dept.getParentId()) {
            dept.setAncestors(ROOT_ANCESTORS);
        } else {
            Dept parentDept = this.getById(dept.getParentId());
            if (parentDept == null) {
                throw new BusinessException("父部门不存在, parentId=" + dept.getParentId());
            }
            dept.setAncestors(parentDept.getAncestors() + "," + parentDept.getId());
        }
        this.save(dept);
        log.info("部门创建成功, id={}", dept.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptUpdateDTO dto) {
        log.info("更新部门, id={}", dto.getId());
        Dept dept = this.getById(dto.getId());
        if (dept == null) {
            throw new BusinessException("部门不存在, id=" + dto.getId());
        }

        // 记录旧的 ancestors，用于判断是否需要更新子部门
        String oldAncestors = dept.getAncestors();

        // 更新字段
        if (dto.getDeptName() != null) {
            dept.setDeptName(dto.getDeptName());
        }
        if (dto.getLeader() != null) {
            dept.setLeader(dto.getLeader());
        }
        if (dto.getPhone() != null) {
            dept.setPhone(dto.getPhone());
        }
        if (dto.getSortOrder() != null) {
            dept.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            dept.setStatus(dto.getStatus().byteValue());
        }

        // 处理 parentId 变更
        if (dto.getParentId() != null && !dto.getParentId().equals(dept.getParentId())) {
            Long newParentId = dto.getParentId();
            // 校验不能将部门移到自身或其子部门下
            if (newParentId.equals(dept.getId())) {
                throw new BusinessException("不能将部门设为自身的子部门");
            }

            String newAncestors;
            if (ROOT_PARENT_ID == newParentId) {
                newAncestors = ROOT_ANCESTORS;
            } else {
                Dept newParentDept = this.getById(newParentId);
                if (newParentDept == null) {
                    throw new BusinessException("父部门不存在, parentId=" + newParentId);
                }
                // 校验新父部门不能是当前部门的子部门
                if (newParentDept.getAncestors() != null
                        && newParentDept.getAncestors().contains("," + dept.getId() + ",")) {
                    throw new BusinessException("不能将部门移动到其子部门下");
                }
                newAncestors = newParentDept.getAncestors() + "," + newParentDept.getId();
            }

            dept.setParentId(newParentId);
            dept.setAncestors(newAncestors);

            // 批量更新所有子部门的 ancestors
            String oldChildPrefix = oldAncestors + "," + dept.getId();
            String newChildPrefix = newAncestors + "," + dept.getId();
            baseMapper.updateChildAncestors(oldChildPrefix, newChildPrefix);
            log.info("已更新子部门 ancestors, 旧前缀={}, 新前缀={}", oldChildPrefix, newChildPrefix);
        }

        this.updateById(dept);
        log.info("部门更新成功, id={}", dept.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long deptId) {
        log.info("删除部门, deptId={}", deptId);
        Dept dept = this.getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        long childCount = this.count(new LambdaQueryWrapper<Dept>()
                .eq(Dept::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }

        // 检查是否有用户关联
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getDeptId, deptId));
        if (userCount > 0) {
            throw new BusinessException("部门下存在用户，无法删除");
        }

        this.removeById(deptId);
        log.info("部门删除成功, deptId={}", deptId);
    }

    /**
     * 将部门实体转换为树形VO
     *
     * @param dept 部门实体
     * @return 部门树VO
     */
    private DeptTreeVO convertToDeptTreeVO(Dept dept) {
        DeptTreeVO vo = new DeptTreeVO();
        vo.setId(dept.getId());
        vo.setParentId(dept.getParentId());
        vo.setDeptName(dept.getDeptName());
        vo.setLeader(dept.getLeader());
        vo.setPhone(dept.getPhone());
        vo.setSortOrder(dept.getSortOrder());
        vo.setStatus(dept.getStatus() != null ? dept.getStatus().intValue() : null);
        return vo;
    }

    /**
     * 将平铺的 VO 列表组装为树形结构
     *
     * @param voList 平铺的部门VO列表
     * @return 树形部门VO列表
     */
    private List<DeptTreeVO> buildTree(List<DeptTreeVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }
        // 按 parentId 分组
        Map<Long, List<DeptTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(DeptTreeVO::getParentId));
        // 为每个节点设置 children
        voList.forEach(vo -> vo.setChildren(parentMap.getOrDefault(vo.getId(), new ArrayList<>())));
        // 返回根节点列表
        return voList.stream()
                .filter(vo -> ROOT_PARENT_ID == vo.getParentId())
                .collect(Collectors.toList());
    }
}
