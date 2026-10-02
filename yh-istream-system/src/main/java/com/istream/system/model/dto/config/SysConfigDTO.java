package com.istream.system.model.dto.config;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysConfigDTO {

    private Long id;

    private String configName;

    private String configKey;

    private String configValue;

    private Integer configType;

    private String remark;

    private LocalDateTime createTime;
}