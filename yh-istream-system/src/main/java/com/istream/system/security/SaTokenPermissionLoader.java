package com.istream.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.istream.framework.cache.CacheService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
import com.istream.system.entity.SysRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.istream.common.constant.Constants.ROLE_CACHE_PREFIX;

/**
 * Sa-Token 权限加载实现
 *
 * <p>委托 {@link SysMenuService} 和 {@link SysUserService} 获取权限与角色，
 * 缓存由 Service 层统一管理，避免双写不一致。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Component
@RequiredArgsConstructor
public class SaTokenPermissionLoader implements StpInterface {

    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;
    private final CacheService cacheService;

    private static final Duration ROLE_CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return sysMenuService.getPermissionsByUserId(Long.parseLong(String.valueOf(loginId)));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        String cacheKey = ROLE_CACHE_PREFIX + userId;
        List<String> cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<String> roles = sysUserService.getRolesByUserId(userId).stream()
                .map(SysRole::getRoleKey)
                .toList();
        cacheService.set(cacheKey, roles, ROLE_CACHE_TTL);
        return roles;
    }
}