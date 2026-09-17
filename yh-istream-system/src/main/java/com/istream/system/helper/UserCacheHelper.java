package com.istream.system.helper;

import com.istream.common.constant.Constants;
import com.istream.framework.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户缓存统一管理
 *
 * <p>集中管理用户相关的权限缓存、角色缓存、数据权限缓存的清除逻辑，
 * 确保所有 Service 调用同一入口，避免缓存不一致。</p>
 *
 * @author istream
 * @since 2026-09-13
 */
@Component
@RequiredArgsConstructor
public class UserCacheHelper {

    private final CacheService cacheService;

    /**
     * 清除单个用户的所有缓存（权限 + 角色 + 数据权限）
     *
     * @param userId 用户ID
     */
    public void evictAll(Long userId) {
        cacheService.delete(Constants.PERM_CACHE_PREFIX + userId);
        cacheService.delete(Constants.ROLE_CACHE_PREFIX + userId);
        cacheService.delete(Constants.DATA_SCOPE_CACHE_PREFIX + userId);
    }

    /**
     * 批量清除多个用户的所有缓存（使用 Pipeline 减少网络往返）
     *
     * @param userIds 用户ID集合
     */
    public void evictAllBatch(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<String> keys = new ArrayList<>(userIds.size() * 3);
        for (Long userId : userIds) {
            keys.add(Constants.PERM_CACHE_PREFIX + userId);
            keys.add(Constants.ROLE_CACHE_PREFIX + userId);
            keys.add(Constants.DATA_SCOPE_CACHE_PREFIX + userId);
        }
        cacheService.deleteBatch(keys);
    }
}