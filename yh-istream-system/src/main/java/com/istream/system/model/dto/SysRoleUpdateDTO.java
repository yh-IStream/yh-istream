package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色修改 DTO
 *
 * <p>仅包含修改角色时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysRoleUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @Size(max = 50, message = "角色标识长度不能超过50")
    private String roleKey;

    private Integer roleSort;

    private Integer dataScope;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}