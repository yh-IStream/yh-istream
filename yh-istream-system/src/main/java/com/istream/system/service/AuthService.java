package com.istream.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.exception.BusinessException;
import com.istream.system.model.dto.auth.LoginDTO;
import com.istream.system.model.dto.user.SysUserDTO;
import com.istream.common.model.sse.SseEvent;
import com.istream.system.model.vo.auth.CaptchaVO;
import com.istream.system.model.vo.auth.LoginVO;
import com.istream.system.model.vo.auth.UserInfoVO;
import com.istream.framework.sse.SseService;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.cache.CacheService;
import com.istream.framework.util.IpRegionUtils;
import com.istream.framework.util.IpUtils;
import com.istream.system.converter.SysUserConverter;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.converter.SysMenuConverter;
import com.istream.system.model.dto.menu.SysMenuDTO;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 认证业务服务
 *
 * <p>封装登录、登出、验证码、用户信息等认证相关业务逻辑</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final SysLoginInfoService sysLoginInfoService;
    private final SysUserConverter sysUserConverter;
    private final SysMenuConverter sysMenuConverter;
    private final CacheService cacheService;
    private final SseService sseService;

    @Value("${captcha.enabled:true}")
    private boolean captchaEnabled;

    private static final Duration CAPTCHA_TTL = Duration.ofSeconds(Constants.CAPTCHA_EXPIRE_SECONDS);

    private static final int USERNAME_ENUM_THRESHOLD_MULTIPLIER = 2;

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @param request  HTTP 请求
     * @return 登录成功后的令牌信息
     */
    public LoginVO login(LoginDTO loginDTO, HttpServletRequest request) {
        validateCaptcha(loginDTO);
        checkLoginFailCount(loginDTO);

        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
        String clientIp = IpUtils.getClientIp(request);
        String loginLocation = IpRegionUtils.parseRegion(clientIp);

        if (user == null) {
            handleUserNotFound(loginDTO, clientIp, loginLocation);
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        checkUserStatus(user, loginDTO.getUsername(), clientIp, loginLocation);
        checkPassword(user, loginDTO.getPassword(), loginDTO.getUsername(), clientIp, loginLocation);

        return performLogin(user, clientIp, loginDTO.getUsername(), loginLocation);
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 生成图形验证码
     *
     * @return 验证码信息（UUID + Base64 图片）
     */
    public CaptchaVO generateCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        cacheService.set(Constants.CAPTCHA_CACHE_PREFIX + uuid,
                lineCaptcha.getCode(), CAPTCHA_TTL);

        return new CaptchaVO(uuid, lineCaptcha.getImageBase64Data());
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息（用户、权限、角色、菜单）
     */
    public UserInfoVO getCurrentUserInfo() {
        Long userId = SecurityUtils.requireLoginUserId();

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        SysUserDTO userDTO = sysUserConverter.toDto(user);

        List<String> permissions = sysMenuService.getPermissionsByUserId(userId);
        List<String> roles = sysUserService.getRolesByUserId(userId).stream()
                .map(SysRole::getRoleKey)
                .toList();
        List<SysMenuDTO> menus = sysMenuConverter.toDtoList(sysMenuService.getCurrentUserMenuTree());

        return UserInfoVO.builder()
                .user(userDTO)
                .permissions(permissions)
                .roles(roles)
                .menus(menus)
                .build();
    }

    private void validateCaptcha(LoginDTO loginDTO) {
        if (!captchaEnabled) {
            return;
        }
        if (StrUtil.isBlank(loginDTO.getCaptchaKey())) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }
        String redisKey = Constants.CAPTCHA_CACHE_PREFIX + loginDTO.getCaptchaKey();
        String cachedCode = cacheService.getAndDelete(redisKey);
        if (cachedCode == null) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }
        if (!cachedCode.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
    }

    private void checkLoginFailCount(LoginDTO loginDTO) {
        String failKey = Constants.LOGIN_FAIL_PREFIX + loginDTO.getUsername();
        long redisFailCount = cacheService.getCounter(failKey);
        if (redisFailCount >= Constants.MAX_LOGIN_FAIL_COUNT) {
            long remain = cacheService.remainTimeToLive(failKey);
            long remainSeconds = remain > 0 ? remain / 1000 : 0;
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR,
                    "账户已被锁定，请" + remainSeconds + "秒后重试");
        }
    }

    private String getFailCounterKey(String username) {
        return Constants.LOGIN_FAIL_PREFIX + username;
    }

    private void handleUserNotFound(LoginDTO loginDTO, String clientIp, String loginLocation) {
        String failKey = getFailCounterKey(loginDTO.getUsername());
        long count = cacheService.incrementAndGet(failKey, Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));

        saveLoginInfo(loginDTO.getUsername(), clientIp, loginLocation, 1, "用户不存在");

        if (count >= Constants.MAX_LOGIN_FAIL_COUNT * USERNAME_ENUM_THRESHOLD_MULTIPLIER) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST,
                    "尝试次数过多，请稍后重试");
        }
        throw new BusinessException(ResultCode.USER_NOT_EXIST);
    }

    private void checkUserStatus(SysUser user, String username, String clientIp, String loginLocation) {
        if (Objects.equals(user.getStatus(), StatusEnum.DISABLED.getCode())) {
            saveLoginInfo(username, clientIp, loginLocation, 1, "用户已停用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String failKey = getFailCounterKey(username);
        long redisFailCount = cacheService.getCounter(failKey);
        if (redisFailCount == 0 && user.getLoginFailCount() != null
                && user.getLoginFailCount() >= Constants.MAX_LOGIN_FAIL_COUNT) {
            cacheService.setCounter(failKey, user.getLoginFailCount().longValue(),
                    Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR,
                    "账户已被锁定，请" + Constants.LOGIN_LOCK_SECONDS + "秒后重试");
        }
    }

    private void checkPassword(SysUser user, String rawPassword, String username, String clientIp, String loginLocation) {
        if (!BCrypt.checkpw(rawPassword, user.getPassword())) {
            String failKey = getFailCounterKey(username);
            long count = cacheService.incrementAndGet(failKey, Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));

            sysUserService.updateLoginFailCount(user.getId(), (int) count);

            saveLoginInfo(username, clientIp, loginLocation, 1, "密码错误（第" + count + "次）");

            if (count >= Constants.MAX_LOGIN_FAIL_COUNT) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR,
                        "密码错误次数过多，账户已锁定" + Constants.LOGIN_LOCK_SECONDS + "秒");
            }
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
    }

    private LoginVO performLogin(SysUser user, String clientIp, String username, String loginLocation) {
        String failKey = getFailCounterKey(username);
        cacheService.deleteCounter(failKey);
        sysUserService.updateLoginFailCount(user.getId(), 0);

        StpUtil.login(user.getId());
        SecurityUtils.setSessionUsername(user.getUsername());

        sysUserService.updateLoginInfo(user.getId(), clientIp);

        saveLoginInfo(username, clientIp, loginLocation, 0, "登录成功");

        return new LoginVO(StpUtil.getTokenValue(), StpUtil.getTokenName());
    }

    private void saveLoginInfo(String username, String clientIp, String loginLocation, int status, String msg) {
        SysLoginInfo loginInfo = new SysLoginInfo();
        loginInfo.setUsername(username);
        loginInfo.setIpAddress(clientIp);
        loginInfo.setLoginLocation(loginLocation);
        loginInfo.setLoginTime(LocalDateTime.now());
        loginInfo.setStatus(status);
        loginInfo.setMsg(msg);
        sysLoginInfoService.save(loginInfo);
        sseService.broadcast(SseEvent.of("LOGIN_INFO", loginInfo));
    }
}