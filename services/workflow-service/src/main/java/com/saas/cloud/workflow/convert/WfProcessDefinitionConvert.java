package com.saas.cloud.workflow.convert;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.saas.cloud.workflow.api.dto.ProcessDefinitionCreateDTO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionDetailVO;
import com.saas.cloud.workflow.api.vo.ProcessDefinitionVO;
import com.saas.cloud.workflow.entity.WfProcessDefinitionExt;

/**
 * 流程定义对象转换器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Mapper(componentModel = "spring")
public interface WfProcessDefinitionConvert {

    WfProcessDefinitionConvert INSTANCE = Mappers.getMapper(WfProcessDefinitionConvert.class);

    /**
     * Entity -> VO
     */
    ProcessDefinitionVO toVO(WfProcessDefinitionExt ext);

    /**
     * Entity -> DetailVO
     */
    ProcessDefinitionDetailVO toDetailVO(WfProcessDefinitionExt ext);

    /**
     * DTO -> Entity
     */
    WfProcessDefinitionExt toEntity(ProcessDefinitionCreateDTO dto);

    /**
     * DTO 属性更新到已有 Entity（忽略 id）
     */
    void updateEntity(ProcessDefinitionCreateDTO dto, @MappingTarget WfProcessDefinitionExt ext);

}
