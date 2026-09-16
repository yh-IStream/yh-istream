package com.istream.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型新增 DTO
 *
 * <p>仅包含新增字典类型时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysDictTypeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}