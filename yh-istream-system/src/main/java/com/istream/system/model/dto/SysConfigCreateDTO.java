package com.istream.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置新增 DTO
 *
 * <p>仅包含新增配置时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysConfigCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    @NotBlank(message = "配置键名不能为空")
    @Size(max = 100, message = "配置键名长度不能超过100")
    private String configKey;

    @NotBlank(message = "配置键值不能为空")
    @Size(max = 500, message = "配置键值长度不能超过500")
    private String configValue;

    private Integer configType;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}