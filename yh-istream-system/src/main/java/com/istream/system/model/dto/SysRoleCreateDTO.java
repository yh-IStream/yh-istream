package com.istream.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色新增 DTO
 *
 * <p>仅包含新增角色时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysRoleCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    @Size(max = 50, message = "角色标识长度不能超过50")
    private String roleKey;

    @NotNull(message = "排序不能为空")
    private Integer roleSort;

    private Integer dataScope;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}