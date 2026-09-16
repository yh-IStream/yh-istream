package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门修改 DTO
 *
 * <p>仅包含修改部门时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysDeptUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "部门ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private Long parentId;

    @Size(max = 100, message = "部门名称长度不能超过100")
    private String deptName;

    private Integer orderNum;

    @Size(max = 50, message = "负责人长度不能超过50")
    private String leader;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}