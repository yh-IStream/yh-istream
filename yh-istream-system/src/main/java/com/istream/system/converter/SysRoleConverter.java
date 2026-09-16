package com.istream.system.converter;

import com.istream.common.enums.DataScopeEnum;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.dto.SysRoleCreateDTO;
import com.istream.system.model.dto.SysRoleDTO;
import com.istream.system.model.dto.SysRoleUpdateDTO;
import com.istream.system.entity.SysRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SysRole DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DataScopeEnum.class, StatusEnum.class})
public interface SysRoleConverter {

    SysRoleDTO toDto(SysRole entity);

    @Mapping(target = "dataScope", expression = "java(dto.getDataScope() != null ? dto.getDataScope() : DataScopeEnum.SELF.getCode())")
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode())")
    SysRole toEntity(SysRoleCreateDTO dto);

    void updateEntity(@MappingTarget SysRole entity, SysRoleUpdateDTO dto);
}