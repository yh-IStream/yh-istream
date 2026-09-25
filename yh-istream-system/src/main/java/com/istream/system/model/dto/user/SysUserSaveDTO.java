package com.istream.system.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.istream.common.validation.Groups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 * <p>修改时前端不传 password 字段，Service 层不读取该字段。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysUserSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(groups = Groups.Update.class, message = "用户ID不能为空")
    private Long id;

    @NotBlank(groups = Groups.Create.class, message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @NotBlank(groups = Groups.Create.class, message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(groups = Groups.Create.class, message = "昵称不能为空")
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