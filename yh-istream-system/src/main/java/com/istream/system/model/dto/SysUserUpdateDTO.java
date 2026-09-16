package com.istream.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户修改 DTO
 *
 * <p>仅包含修改用户时允许传入的字段，不包含密码字段（密码修改需通过专用重置接口），</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
public class SysUserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    private Long deptId;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    private Integer gender;

    private Integer status;

    private List<Long> roleIds;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}