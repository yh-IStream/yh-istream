package com.istream.system.converter;

import com.istream.common.constant.Constants;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.dto.dict.SysDictDataSaveDTO;
import com.istream.system.model.dto.dict.SysDictDataDTO;
import com.istream.system.entity.SysDictData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SysDictData DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Constants.class, StatusEnum.class})
public interface SysDictDataConverter {

    SysDictDataDTO toDto(SysDictData entity);

    @Mapping(target = "isDefault", expression = "java(dto.getIsDefault() != null ? dto.getIsDefault() : Constants.DEFAULT_NOT_DEFAULT)")
    @Mapping(target = "orderNum", expression = "java(dto.getOrderNum() != null ? dto.getOrderNum() : Constants.DEFAULT_ORDER_NUM)")
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode())")
    SysDictData toEntity(SysDictDataSaveDTO dto);

    void updateEntity(@MappingTarget SysDictData entity, SysDictDataSaveDTO dto);
}