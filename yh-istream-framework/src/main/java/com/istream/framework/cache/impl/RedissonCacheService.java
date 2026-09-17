package com.istream.framework.cache.impl;

import com.istream.framework.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;

/**
 * 基于 Redisson 的缓存服务实现
 *
 * <p>统一封装 Redisson 的 RBucket 和 RAtomicLong 操作，
 * 提供一致的缓存访问接口。</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonCacheService implements CacheService {

    private final RedissonClient redissonClient;

    @Override
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public void set(String key, Object value, Duration duration) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }

    @Override
    public void set(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    @Override
    public void delete(String key) {
        redissonClient.getBucket(key).delete();
    }

    @Override
    public void deleteBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        RBatch batch = redissonClient.createBatch();
        for (String key : keys) {
            batch.getBucket(key).deleteAsync();
        }
        batch.execute();
    }

    @Override
    public void deleteByPattern(String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    @Override
    public long incrementAndGet(String key, Duration duration) {
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        long value = counter.incrementAndGet();
        if (counter.remainTimeToLive() < 0) {
            counter.expire(duration);
        }
        return value;
    }

    @Override
    public long getCounter(String key) {
        return redissonClient.getAtomicLong(key).get();
    }

    @Override
    public void setCounter(String key, long value, Duration duration) {
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        counter.set(value);
        counter.expire(duration);
    }

    @Override
    public void deleteCounter(String key) {
        redissonClient.getAtomicLong(key).delete();
    }

    @Override
    public long remainTimeToLive(String key) {
        return redissonClient.getAtomicLong(key).remainTimeToLive();
    }

    @Override
    public <T> T getAndDelete(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.getAndDelete();
    }
}