package com.istream.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.dto.LoginDTO;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.common.util.IpUtils;
import com.istream.framework.security.SecurityUtils;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final SysLoginInfoService sysLoginInfoService;
    private final HttpServletRequest request;

    @OperLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());

        SysLoginInfo loginInfo = new SysLoginInfo();
        loginInfo.setUsername(loginDTO.getUsername());
        loginInfo.setIpAddress(IpUtils.getClientIp(request));
        loginInfo.setLoginTime(LocalDateTime.now());

        if (user == null) {
            loginInfo.setStatus(1);
            loginInfo.setMsg("用户不存在");
            sysLoginInfoService.save(loginInfo);
            return R.fail(ResultCode.USER_NOT_EXIST);
        }
        if (Objects.equals(user.getStatus(), 1)) {
            loginInfo.setStatus(1);
            loginInfo.setMsg("用户已停用");
            sysLoginInfoService.save(loginInfo);
            return R.fail(ResultCode.USER_DISABLED);
        }
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            loginInfo.setStatus(1);
            loginInfo.setMsg("密码错误");
            sysLoginInfoService.save(loginInfo);
            return R.fail(ResultCode.USER_PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());

        sysUserService.updateLoginInfo(user.getId(), IpUtils.getClientIp(request));

        loginInfo.setStatus(0);
        loginInfo.setMsg("登录成功");
        sysLoginInfoService.save(loginInfo);

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", StpUtil.getTokenValue());
        tokenInfo.put("tokenName", StpUtil.getTokenName());
        return R.ok(tokenInfo);
    }

    @OperLog(title = "用户登出", businessType = BusinessType.LOGOUT)
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = SecurityUtils.getLoginUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return R.fail(ResultCode.USER_NOT_EXIST);
        }

        List<String> permissions = sysMenuService.getPermissionsByUserId(userId);
        List<SysMenu> menus = sysMenuService.listMenuTree();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user", user);
        userInfo.put("permissions", permissions);
        userInfo.put("menus", menus);
        return R.ok(userInfo);
    }
}