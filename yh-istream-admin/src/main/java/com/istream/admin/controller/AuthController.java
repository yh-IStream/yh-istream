package com.istream.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.constant.Constants;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.dto.LoginDTO;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.util.IpUtils;
import com.istream.framework.sse.SseService;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.util.IpRegionUtils;
import com.istream.common.model.sse.SseEvent;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final SysLoginInfoService sysLoginInfoService;
    private final HttpServletRequest request;
    private final RedissonClient redissonClient;
    private final SseService sseService;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

    @OperLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @RateLimit(key = "login", rate = 5, timeout = 0)
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        if (StrUtil.isNotBlank(loginDTO.getCaptchaKey())) {
            String redisKey = CAPTCHA_PREFIX + loginDTO.getCaptchaKey();
            String cachedCode = redissonClient.<String>getBucket(redisKey).getAndDelete();
            if (cachedCode == null) {
                return R.fail(ResultCode.PARAM_VALID_ERROR, "验证码已过期");
            }
            if (!cachedCode.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
                return R.fail(ResultCode.PARAM_VALID_ERROR, "验证码错误");
            }
        }

        String failKey = LOGIN_FAIL_PREFIX + loginDTO.getUsername();
        RAtomicLong failCount = redissonClient.getAtomicLong(failKey);
        long redisFailCount = failCount.get();
        if (redisFailCount >= Constants.MAX_LOGIN_FAIL_COUNT) {
            long remain = failCount.remainTimeToLive();
            long remainSeconds = remain > 0 ? remain / 1000 : 0;
            return R.fail(ResultCode.USER_PASSWORD_ERROR,
                    "账户已被锁定，请" + remainSeconds + "秒后重试");
        }

        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());

        if (user != null && redisFailCount == 0 && user.getLoginFailCount() != null
                && user.getLoginFailCount() >= Constants.MAX_LOGIN_FAIL_COUNT) {
            failCount.set(user.getLoginFailCount().longValue());
            failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));
            return R.fail(ResultCode.USER_PASSWORD_ERROR,
                    "账户已被锁定，请" + Constants.LOGIN_LOCK_SECONDS + "秒后重试");
        }

        SysLoginInfo loginInfo = new SysLoginInfo();
        loginInfo.setUsername(loginDTO.getUsername());
        loginInfo.setIpAddress(IpUtils.getClientIp(request));
        loginInfo.setLoginLocation(IpRegionUtils.parseRegion(loginInfo.getIpAddress()));
        loginInfo.setLoginTime(LocalDateTime.now());

        if (user == null) {
            long count = failCount.incrementAndGet();
            failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));
            loginInfo.setStatus(1);
            loginInfo.setMsg("用户不存在");
            sysLoginInfoService.save(loginInfo);
            sseService.broadcast(SseEvent.of("LOGIN_INFO", loginInfo));
            if (count >= Constants.MAX_LOGIN_FAIL_COUNT * 2) {
                return R.fail(ResultCode.USER_NOT_EXIST,
                        "尝试次数过多，请稍后重试");
            }
            return R.fail(ResultCode.USER_NOT_EXIST);
        }
        if (Objects.equals(user.getStatus(), StatusEnum.DISABLED.getCode())) {
            loginInfo.setStatus(1);
            loginInfo.setMsg("用户已停用");
            sysLoginInfoService.save(loginInfo);
            sseService.broadcast(SseEvent.of("LOGIN_INFO", loginInfo));
            return R.fail(ResultCode.USER_DISABLED);
        }
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            long count = failCount.incrementAndGet();
            failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));
            user.setLoginFailCount((int) count);
            sysUserService.updateLoginFailCount(user.getId(), (int) count);
            loginInfo.setStatus(1);
            loginInfo.setMsg("密码错误（第" + count + "次）");
            sysLoginInfoService.save(loginInfo);
            if (count >= Constants.MAX_LOGIN_FAIL_COUNT) {
                return R.fail(ResultCode.USER_PASSWORD_ERROR,
                        "密码错误次数过多，账户已锁定" + Constants.LOGIN_LOCK_SECONDS + "秒");
            }
            return R.fail(ResultCode.USER_PASSWORD_ERROR);
        }

        failCount.delete();
        sysUserService.updateLoginFailCount(user.getId(), 0);

        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());

        sysUserService.updateLoginInfo(user.getId(), IpUtils.getClientIp(request));

        loginInfo.setStatus(0);
        loginInfo.setMsg("登录成功");
        sysLoginInfoService.save(loginInfo);
        sseService.broadcast(SseEvent.of("LOGIN_INFO", loginInfo));

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

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        redissonClient.getBucket(CAPTCHA_PREFIX + uuid).set(lineCaptcha.getCode(), CAPTCHA_TTL);

        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", lineCaptcha.getImageBase64Data());
        return R.ok(result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = SecurityUtils.getLoginUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return R.fail(ResultCode.USER_NOT_EXIST);
        }
        user.setPassword(null);

        List<String> permissions = sysMenuService.getPermissionsByUserId(userId);
        List<String> roles = sysUserService.getRolesByUserId(userId).stream()
                .map(SysRole::getRoleKey)
                .toList();
        List<SysMenu> menus = sysMenuService.getCurrentUserMenuTree();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user", user);
        userInfo.put("permissions", permissions);
        userInfo.put("roles", roles);
        userInfo.put("menus", menus);
        return R.ok(userInfo);
    }
}