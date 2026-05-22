package com.saas.cloud.workflow.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.saas.cloud.workflow.api.dto.NodeConfigDTO;
import com.saas.cloud.workflow.api.vo.NodeConfigVO;
import com.saas.cloud.workflow.entity.WfNodeConfig;

/**
 * 节点配置对象转换器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Mapper(componentModel = "spring")
public interface WfNodeConfigConvert {

    WfNodeConfigConvert INSTANCE = Mappers.getMapper(WfNodeConfigConvert.class);

    /**
     * Entity -> VO
     */
    NodeConfigVO toVO(WfNodeConfig entity);

    /**
     * Entity List -> VO List
     */
    List<NodeConfigVO> toVOList(List<WfNodeConfig> entities);

    /**
     * DTO -> Entity
     */
    WfNodeConfig toEntity(NodeConfigDTO dto);

    /**
     * DTO List -> Entity List
     */
    List<WfNodeConfig> toEntityList(List<NodeConfigDTO> dtos);

}
