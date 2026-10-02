package com.istream.system.model.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应 VO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {

    private String uuid;

    private String image;
}