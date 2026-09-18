package com.istream.system.converter;

import com.istream.system.model.dto.operlog.SysOperLogDTO;
import com.istream.system.entity.SysOperLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * SysOperLog DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysOperLogConverter {

    SysOperLogDTO toDto(SysOperLog entity);
}