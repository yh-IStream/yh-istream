package com.istream.system.model.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应 VO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private String tokenName;
}