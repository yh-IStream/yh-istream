package com.istream.system.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String captchaCode;

    private String captchaKey;
}