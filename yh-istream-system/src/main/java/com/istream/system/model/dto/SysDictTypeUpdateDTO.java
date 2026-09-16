package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型修改 DTO
 *
 * <p>仅包含修改字典类型时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysDictTypeUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "字典类型ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;

    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}