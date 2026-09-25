package com.istream.system.controller;

import com.istream.system.service.AuthService;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.BusinessType;
import com.istream.system.model.dto.auth.LoginDTO;
import com.istream.common.model.R;
import com.istream.system.model.vo.auth.CaptchaVO;
import com.istream.system.model.vo.auth.LoginVO;
import com.istream.system.model.vo.auth.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证管理控制器
 *
 * <p>负责登录、登出、验证码、用户信息等认证相关接口</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @OperLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @RateLimit(key = "login", rate = 5, timeout = 0)
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        return R.ok(authService.login(loginDTO, request));
    }

    @OperLog(title = "用户登出", businessType = BusinessType.LOGOUT)
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @RateLimit(key = "captcha", rate = 10, timeout = 0)
    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.generateCaptcha());
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<UserInfoVO> getUserInfo() {
        return R.ok(authService.getCurrentUserInfo());
    }
}