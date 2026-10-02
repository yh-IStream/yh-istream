package com.istream.system.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求 DTO
 *
 * @author istream
 * @since 2026-09-18
 */
@Data
public class SysUserResetPasswordDTO {

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}