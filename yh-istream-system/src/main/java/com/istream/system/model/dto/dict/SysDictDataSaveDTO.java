package com.istream.system.model.dto.dict;

import com.istream.common.validation.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysDictDataSaveDTO {

    @NotNull(groups = Groups.Update.class, message = "字典数据ID不能为空")
    private Long id;

    @NotBlank(groups = Groups.Create.class, message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    @NotBlank(groups = Groups.Create.class, message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100")
    private String dictLabel;

    @NotBlank(groups = Groups.Create.class, message = "字典值不能为空")
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