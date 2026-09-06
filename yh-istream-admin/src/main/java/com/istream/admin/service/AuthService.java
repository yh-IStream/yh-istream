package com.istream.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.dto.LoginDTO;
import com.istream.common.model.dto.SysUserDTO;
import com.istream.common.model.sse.SseEvent;
import com.istream.common.model.vo.CaptchaVO;
import com.istream.common.model.vo.LoginVO;
import com.istream.common.model.vo.UserInfoVO;
import com.istream.framework.sse.SseService;
import com.istream.framework.util.IpRegionUtils;
import com.istream.framework.util.IpUtils;
import com.istream.system.converter.SysUserConverter;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 认证业务服务
 *
 * <p>封装登录、登出、验证码、用户信息等认证相关业务逻辑，
 * 从 AuthController 中抽取，遵循 Controller-Service 分层原则。</p>
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
    private final RedissonClient redissonClient;
    private final SseService sseService;

    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

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
        }

        Objects.requireNonNull(user, "用户查询结果不应为null，前置校验已通过");
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

        redissonClient.getBucket(Constants.CAPTCHA_CACHE_PREFIX + uuid)
                .set(lineCaptcha.getCode(), CAPTCHA_TTL);

        return new CaptchaVO(uuid, lineCaptcha.getImageBase64Data());
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息（用户、权限、角色、菜单）
     */
    public UserInfoVO getCurrentUserInfo() {
        Long userId = com.istream.framework.security.SecurityUtils.getLoginUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        SysUserDTO userDTO = sysUserConverter.toDto(user);

        List<String> permissions = sysMenuService.getPermissionsByUserId(userId);
        List<String> roles = sysUserService.getRolesByUserId(userId).stream()
                .map(SysRole::getRoleKey)
                .toList();
        List<SysMenu> menus = sysMenuService.getCurrentUserMenuTree();

        return UserInfoVO.builder()
                .user(userDTO)
                .permissions(permissions)
                .roles(roles)
                .menus(menus)
                .build();
    }

    private void validateCaptcha(LoginDTO loginDTO) {
        if (StrUtil.isNotBlank(loginDTO.getCaptchaKey())) {
            String redisKey = Constants.CAPTCHA_CACHE_PREFIX + loginDTO.getCaptchaKey();
            String cachedCode = redissonClient.<String>getBucket(redisKey).getAndDelete();
            if (cachedCode == null) {
                throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "验证码已过期");
            }
            if (!cachedCode.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
                throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "验证码错误");
            }
        }
    }

    private void checkLoginFailCount(LoginDTO loginDTO) {
        String failKey = Constants.LOGIN_FAIL_PREFIX + loginDTO.getUsername();
        RAtomicLong failCount = redissonClient.getAtomicLong(failKey);
        long redisFailCount = failCount.get();
        if (redisFailCount >= Constants.MAX_LOGIN_FAIL_COUNT) {
            long remain = failCount.remainTimeToLive();
            long remainSeconds = remain > 0 ? remain / 1000 : 0;
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(),
                    "账户已被锁定，请" + remainSeconds + "秒后重试");
        }
    }

    private RAtomicLong getFailCounter(String username) {
        return redissonClient.getAtomicLong(Constants.LOGIN_FAIL_PREFIX + username);
    }

    private void handleUserNotFound(LoginDTO loginDTO, String clientIp, String loginLocation) {
        RAtomicLong failCount = getFailCounter(loginDTO.getUsername());
        long count = failCount.incrementAndGet();
        failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));

        saveLoginInfo(loginDTO.getUsername(), clientIp, loginLocation, 1, "用户不存在");

        if (count >= Constants.MAX_LOGIN_FAIL_COUNT * 2) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST.getCode(),
                    "尝试次数过多，请稍后重试");
        }
        throw new BusinessException(ResultCode.USER_NOT_EXIST);
    }

    private void checkUserStatus(SysUser user, String username, String clientIp, String loginLocation) {
        if (Objects.equals(user.getStatus(), StatusEnum.DISABLED.getCode())) {
            saveLoginInfo(username, clientIp, loginLocation, 1, "用户已停用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        RAtomicLong failCount = getFailCounter(username);
        long redisFailCount = failCount.get();
        if (redisFailCount == 0 && user.getLoginFailCount() != null
                && user.getLoginFailCount() >= Constants.MAX_LOGIN_FAIL_COUNT) {
            failCount.set(user.getLoginFailCount().longValue());
            failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(),
                    "账户已被锁定，请" + Constants.LOGIN_LOCK_SECONDS + "秒后重试");
        }
    }

    private void checkPassword(SysUser user, String rawPassword, String username, String clientIp, String loginLocation) {
        if (!BCrypt.checkpw(rawPassword, user.getPassword())) {
            RAtomicLong failCount = getFailCounter(username);
            long count = failCount.incrementAndGet();
            failCount.expire(Duration.ofSeconds(Constants.LOGIN_LOCK_SECONDS));

            user.setLoginFailCount((int) count);
            sysUserService.updateLoginFailCount(user.getId(), (int) count);

            saveLoginInfo(username, clientIp, loginLocation, 1, "密码错误（第" + count + "次）");

            if (count >= Constants.MAX_LOGIN_FAIL_COUNT) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(),
                        "密码错误次数过多，账户已锁定" + Constants.LOGIN_LOCK_SECONDS + "秒");
            }
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
    }

    private LoginVO performLogin(SysUser user, String clientIp, String username, String loginLocation) {
        RAtomicLong failCount = getFailCounter(username);
        failCount.delete();
        sysUserService.updateLoginFailCount(user.getId(), 0);

        StpUtil.login(user.getId());
        StpUtil.getSession().set(Constants.SESSION_USERNAME_KEY, user.getUsername());

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