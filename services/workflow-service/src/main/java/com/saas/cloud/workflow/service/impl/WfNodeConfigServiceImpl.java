package com.saas.cloud.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.workflow.api.dto.NodeConfigDTO;
import com.saas.cloud.workflow.api.vo.NodeConfigVO;
import com.saas.cloud.workflow.convert.WfNodeConfigConvert;
import com.saas.cloud.workflow.entity.WfNodeConfig;
import com.saas.cloud.workflow.mapper.WfNodeConfigMapper;
import com.saas.cloud.workflow.service.IWfNodeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程节点审批人配置表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WfNodeConfigServiceImpl
        extends ServiceImpl<WfNodeConfigMapper, WfNodeConfig>
        implements IWfNodeConfigService {

    private final WfNodeConfigConvert nodeConfigConvert;

    /** 审批人类型：指定用户 */
    private static final byte ASSIGNEE_TYPE_USER = 1;
    /** 审批人类型：指定角色 */
    private static final byte ASSIGNEE_TYPE_ROLE = 2;
    /** 审批人类型：部门负责人 */
    private static final byte ASSIGNEE_TYPE_DEPT_LEADER = 3;
    /** 审批人类型：发起人自选 */
    private static final byte ASSIGNEE_TYPE_SELF_SELECT = 4;

    /** 审批模式：或签 */
    private static final byte APPROVAL_MODE_OR = 1;
    /** 审批模式：会签 */
    private static final byte APPROVAL_MODE_AND = 2;
    /** 审批模式：依次 */
    private static final byte APPROVAL_MODE_SEQUENTIAL = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNodeConfigs(String processDefinitionId, List<NodeConfigDTO> configs) {
        // 先删除已有配置
        LambdaQueryWrapper<WfNodeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfNodeConfig::getProcessDefinitionId, processDefinitionId);
        remove(wrapper);

        // 批量插入新配置
        if (CollUtil.isNotEmpty(configs)) {
            List<WfNodeConfig> entityList = configs.stream()
                    .map(dto -> {
                        WfNodeConfig entity = nodeConfigConvert.toEntity(dto);
                        entity.setProcessDefinitionId(processDefinitionId);
                        return entity;
                    })
                    .collect(Collectors.toList());
            saveBatch(entityList);
            log.info("保存节点配置成功, processDefinitionId={}, count={}", processDefinitionId, entityList.size());
        }
    }

    @Override
    public List<NodeConfigVO> getNodeConfigs(String processDefinitionId) {
        LambdaQueryWrapper<WfNodeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfNodeConfig::getProcessDefinitionId, processDefinitionId);
        List<WfNodeConfig> list = list(wrapper);

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转换为VO
     *
     * @param entity 实体
     * @return VO
     */
    private NodeConfigVO convertToVO(WfNodeConfig entity) {
        NodeConfigVO vo = nodeConfigConvert.toVO(entity);
        vo.setAssigneeTypeDesc(getAssigneeTypeDesc(entity.getAssigneeType()));
        vo.setApprovalModeDesc(getApprovalModeDesc(entity.getApprovalMode()));
        return vo;
    }

    /**
     * 获取审批人类型描述
     *
     * @param assigneeType 审批人类型
     * @return 描述
     */
    private String getAssigneeTypeDesc(Byte assigneeType) {
        if (assigneeType == null) {
            return "未知";
        }
        switch (assigneeType) {
            case ASSIGNEE_TYPE_USER:
                return "指定用户";
            case ASSIGNEE_TYPE_ROLE:
                return "指定角色";
            case ASSIGNEE_TYPE_DEPT_LEADER:
                return "部门负责人";
            case ASSIGNEE_TYPE_SELF_SELECT:
                return "发起人自选";
            default:
                return "未知";
        }
    }

    /**
     * 获取审批模式描述
     *
     * @param approvalMode 审批模式
     * @return 描述
     */
    private String getApprovalModeDesc(Byte approvalMode) {
        if (approvalMode == null) {
            return "未知";
        }
        switch (approvalMode) {
            case APPROVAL_MODE_OR:
                return "或签";
            case APPROVAL_MODE_AND:
                return "会签";
            case APPROVAL_MODE_SEQUENTIAL:
                return "依次";
            default:
                return "未知";
        }
    }
}
