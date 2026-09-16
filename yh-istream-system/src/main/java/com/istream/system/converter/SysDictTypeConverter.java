package com.istream.system.converter;

import com.istream.common.enums.StatusEnum;
import com.istream.system.model.dto.SysDictTypeCreateDTO;
import com.istream.system.model.dto.SysDictTypeDTO;
import com.istream.system.model.dto.SysDictTypeUpdateDTO;
import com.istream.system.entity.SysDictType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SysDictType DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = StatusEnum.class)
public interface SysDictTypeConverter {

    SysDictTypeDTO toDto(SysDictType entity);

    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode())")
    SysDictType toEntity(SysDictTypeCreateDTO dto);

    void updateEntity(@MappingTarget SysDictType entity, SysDictTypeUpdateDTO dto);
}