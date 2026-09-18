package com.istream.system.model.dto.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.istream.common.validation.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysConfigSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(groups = Groups.Update.class, message = "配置ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @NotBlank(groups = Groups.Create.class, message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    @NotBlank(groups = Groups.Create.class, message = "配置键名不能为空")
    @Size(max = 100, message = "配置键名长度不能超过100")
    private String configKey;

    @NotBlank(groups = Groups.Create.class, message = "配置键值不能为空")
    @Size(max = 500, message = "配置键值长度不能超过500")
    private String configValue;

    private Integer configType;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}