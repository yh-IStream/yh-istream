package com.istream.system.converter;

import com.istream.system.model.dto.SysUserDTO;
import com.istream.common.converter.BaseConverter;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * SysUser ↔ SysUserDTO 转换器
 *
 * @author istream
 * @since 2026-08-17
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysUserConverter extends BaseConverter<SysUser, SysUserDTO> {

    @Override
    @Mapping(source = "roles", target = "roleNames", qualifiedByName = "mapRoleNames")
    @Mapping(source = "roles", target = "roleIds", qualifiedByName = "mapRoleIds")
    SysUserDTO toDto(SysUser entity);

    @Override
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    SysUser toEntity(SysUserDTO dto);

    @Named("mapRoleNames")
    default List<String> mapRoleNames(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SysRole::getRoleName)
                .toList();
    }

    @Named("mapRoleIds")
    default List<String> mapRoleIds(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> String.valueOf(role.getId()))
                .toList();
    }
}