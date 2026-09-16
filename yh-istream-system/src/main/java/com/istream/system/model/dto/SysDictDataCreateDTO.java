package com.istream.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据新增 DTO
 *
 * <p>仅包含新增字典数据时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysDictDataCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 100, message = "字典值长度不能超过100")
    private String dictValue;

    @Size(max = 100, message = "样式属性长度不能超过100")
    private String cssClass;

    @Size(max = 100, message = "表格回显样式长度不能超过100")
    private String listClass;

    private Integer isDefault;

    private Integer orderNum;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}