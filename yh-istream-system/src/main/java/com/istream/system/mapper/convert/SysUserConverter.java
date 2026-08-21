package com.istream.system.mapper.convert;

import com.istream.common.model.dto.SysUserDTO;
import com.istream.common.model.mapper.BaseConverter;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SysUser ↔ SysUserDTO 转换器
 *
 * @author isteam
 * @since 2026-08-17
 */
@Mapper(componentModel = "spring")
public interface SysUserConverter extends BaseConverter<SysUser, SysUserDTO> {

    @Override
    @Mapping(source = "dept.deptName", target = "deptName")
    @Mapping(source = "roles", target = "roleNames", qualifiedByName = "mapRoleNames")
    SysUserDTO toDto(SysUser entity);

    @Override
    @Mapping(target = "dept", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "remark", ignore = true)
    SysUser toEntity(SysUserDTO dto);

    @Named("mapRoleNames")
    default List<String> mapRoleNames(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toList());
    }
}