package com.istream.system.model.dto.dict;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysDictTypeDTO {

    private Long id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}