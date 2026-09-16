package com.istream.system.converter;

import com.istream.system.model.dto.SysLoginInfoDTO;
import com.istream.system.entity.SysLoginInfo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * SysLoginInfo DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysLoginInfoConverter {

    SysLoginInfoDTO toDto(SysLoginInfo entity);
}