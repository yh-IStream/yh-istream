package com.istream.system.model.dto.role;

import com.istream.common.validation.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysRoleSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(groups = Groups.Update.class, message = "角色ID不能为空")
    private Long id;

    @NotBlank(groups = Groups.Create.class, message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @NotBlank(groups = Groups.Create.class, message = "角色标识不能为空")
    @Size(max = 50, message = "角色标识长度不能超过50")
    private String roleKey;

    @NotNull(groups = Groups.Create.class, message = "排序不能为空")
    private Integer roleSort;

    private Integer dataScope;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}