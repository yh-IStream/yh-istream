package com.istream.framework.cache;

import java.time.Duration;
import java.util.Collection;

/**
 * 统一缓存服务抽象层
 *
 * <p>封装 Redisson 缓存操作，提供一致的缓存策略</p>
 *
 * @author istream
 * @since 2026-09-08
 */
public interface CacheService {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值，不存在时返回 null
     */
    <T> T get(String key);

    /**
     * 设置缓存值（带过期时间）
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 过期时间
     */
    void set(String key, Object value, Duration duration);

    /**
     * 设置缓存值（永不过期）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    void deleteBatch(Collection<String> keys);

    /**
     * 按模式批量删除缓存
     *
     * @param pattern 键模式（如 "config:*"）
     */
    void deleteByPattern(String pattern);

    /**
     * 获取原子长整型计数器并递增
     *
     * @param key      缓存键
     * @param duration 过期时间
     * @return 递增后的值
     */
    long incrementAndGet(String key, Duration duration);

    /**
     * 获取原子长整型计数器当前值
     *
     * @param key 缓存键
     * @return 当前值
     */
    long getCounter(String key);

    /**
     * 设置原子长整型计数器值
     *
     * @param key      缓存键
     * @param value    值
     * @param duration 过期时间
     */
    void setCounter(String key, long value, Duration duration);

    /**
     * 删除原子计数器
     *
     * @param key 缓存键
     */
    void deleteCounter(String key);

    /**
     * 获取计数器剩余存活时间（毫秒）
     *
     * @param key 缓存键
     * @return 剩余时间（毫秒），不存在时返回 -1
     */
    long remainTimeToLive(String key);

    /**
     * 获取并删除缓存值（原子操作，适用于一次性令牌如验证码）
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值，不存在时返回 null
     */
    <T> T getAndDelete(String key);
}