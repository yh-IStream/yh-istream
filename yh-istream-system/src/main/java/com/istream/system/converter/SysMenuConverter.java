package com.istream.system.converter;

import com.istream.common.constant.Constants;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.dto.menu.SysMenuSaveDTO;
import com.istream.system.model.dto.menu.SysMenuDTO;
import com.istream.system.entity.SysMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * SysMenu DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Constants.class, StatusEnum.class})
public interface SysMenuConverter {

    SysMenuDTO toDto(SysMenu entity);

    List<SysMenuDTO> toDtoList(List<SysMenu> entities);

    @Mapping(target = "parentId", expression = "java(dto.getParentId() != null ? dto.getParentId() : Constants.ROOT_PARENT_ID)")
    @Mapping(target = "orderNum", expression = "java(dto.getOrderNum() != null ? dto.getOrderNum() : Constants.DEFAULT_ORDER_NUM)")
    @Mapping(target = "visible", expression = "java(dto.getVisible() != null ? dto.getVisible() : StatusEnum.ENABLED.getCode())")
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode())")
    SysMenu toEntity(SysMenuSaveDTO dto);

    void updateEntity(@MappingTarget SysMenu entity, SysMenuSaveDTO dto);
}