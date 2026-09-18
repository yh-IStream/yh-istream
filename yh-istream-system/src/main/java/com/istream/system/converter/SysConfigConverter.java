package com.istream.system.converter;

import com.istream.common.constant.Constants;
import com.istream.system.model.dto.config.SysConfigSaveDTO;
import com.istream.system.model.dto.config.SysConfigDTO;
import com.istream.system.entity.SysConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SysConfig DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Constants.class)
public interface SysConfigConverter {

    SysConfigDTO toDto(SysConfig entity);

    @Mapping(target = "configType", expression = "java(dto.getConfigType() != null ? dto.getConfigType() : Constants.DEFAULT_CONFIG_TYPE)")
    SysConfig toEntity(SysConfigSaveDTO dto);

    void updateEntity(@MappingTarget SysConfig entity, SysConfigSaveDTO dto);
}