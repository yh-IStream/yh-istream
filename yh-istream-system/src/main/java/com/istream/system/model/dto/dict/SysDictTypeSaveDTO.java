package com.istream.system.model.dto.dict;

import com.istream.common.validation.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysDictTypeSaveDTO {

    @NotNull(groups = Groups.Update.class, message = "字典类型ID不能为空")
    private Long id;

    @NotBlank(groups = Groups.Create.class, message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;

    @NotBlank(groups = Groups.Create.class, message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}