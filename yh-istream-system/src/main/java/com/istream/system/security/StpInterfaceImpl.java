package com.istream.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.istream.common.constant.Constants.PERM_CACHE_PREFIX;
import static com.istream.common.constant.Constants.ROLE_CACHE_PREFIX;

/**
 * Sa-Token 权限加载实现
 *
 * <p>从数据库加载当前用户的角色和权限码集合，使用 Redis 缓存以减少数据库查询压力。
 * 缓存 TTL 为 30 分钟，角色/权限变更时通过 {@code clearUserCache} 主动清除缓存。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final RedissonClient redissonClient;

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String cacheKey = PERM_CACHE_PREFIX + loginId;
        RBucket<List<String>> bucket = redissonClient.getBucket(cacheKey);
        List<String> cached = bucket.get();
        if (cached != null) {
            return cached;
        }
        List<String> permissions = sysMenuMapper.selectPermissionsByUserId(Long.valueOf(loginId.toString()));
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        bucket.set(permissions, CACHE_TTL);
        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String cacheKey = ROLE_CACHE_PREFIX + loginId;
        RBucket<List<String>> bucket = redissonClient.getBucket(cacheKey);
        List<String> cached = bucket.get();
        if (cached != null) {
            return cached;
        }
        List<String> roles = sysRoleMapper.selectRoleKeysByUserId(Long.valueOf(loginId.toString()));
        if (roles == null) {
            roles = Collections.emptyList();
        }
        bucket.set(roles, CACHE_TTL);
        return roles;
    }
}