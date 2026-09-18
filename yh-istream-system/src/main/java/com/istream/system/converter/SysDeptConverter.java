package com.istream.system.converter;

import com.istream.common.constant.Constants;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.dto.dept.SysDeptSaveDTO;
import com.istream.system.model.dto.dept.SysDeptDTO;
import com.istream.system.entity.SysDept;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SysDept DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Constants.class, StatusEnum.class})
public interface SysDeptConverter {

    SysDeptDTO toDto(SysDept entity);

    @Mapping(target = "parentId", expression = "java(dto.getParentId() != null ? dto.getParentId() : Constants.ROOT_PARENT_ID)")
    @Mapping(target = "orderNum", expression = "java(dto.getOrderNum() != null ? dto.getOrderNum() : Constants.DEFAULT_ORDER_NUM)")
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode())")
    SysDept toEntity(SysDeptSaveDTO dto);

    void updateEntity(@MappingTarget SysDept entity, SysDeptSaveDTO dto);
}