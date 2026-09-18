package com.istream.file.converter;

import com.istream.file.model.dto.file.SysFileDTO;
import com.istream.file.entity.SysFile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * SysFile DTO ↔ Entity 转换器
 *
 * @author istream
 * @since 2026-09-15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysFileConverter {

    SysFileDTO toDto(SysFile entity);
}