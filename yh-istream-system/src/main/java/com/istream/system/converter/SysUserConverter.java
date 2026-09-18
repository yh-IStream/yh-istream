package com.istream.system.converter;

import com.istream.system.model.dto.user.SysUserDTO;
import com.istream.system.model.dto.user.SysUserSaveDTO;
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

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "loginIp", ignore = true)
    @Mapping(target = "loginDate", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "loginFailCount", ignore = true)
    @Mapping(target = "pwdResetTime", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "roles", ignore = true)
    SysUser toEntity(SysUserSaveDTO dto);

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