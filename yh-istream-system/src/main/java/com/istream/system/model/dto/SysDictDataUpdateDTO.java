package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据修改 DTO
 *
 * <p>仅包含修改字典数据时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysDictDataUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "字典数据ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    @Size(max = 100, message = "字典标签长度不能超过100")
    private String dictLabel;

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