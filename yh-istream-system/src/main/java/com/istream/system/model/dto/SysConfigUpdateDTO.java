package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置修改 DTO
 *
 * <p>仅包含修改配置时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysConfigUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "配置ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    @Size(max = 100, message = "配置键名长度不能超过100")
    private String configKey;

    @Size(max = 500, message = "配置键值长度不能超过500")
    private String configValue;

    private Integer configType;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}