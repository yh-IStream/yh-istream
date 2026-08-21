package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysConfig;
import com.istream.system.mapper.SysConfigMapper;
import com.istream.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final RedissonClient redissonClient;

    private static final String CONFIG_KEY_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public String getConfigValueByKey(String configKey) {
        String cacheKey = CONFIG_KEY_PREFIX + configKey;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        String cached = bucket.get();
        if (cached != null) {
            return cached;
        }
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        String value = config != null ? config.getConfigValue() : null;
        if (value != null) {
            bucket.set(value, CACHE_TTL);
        }
        return value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysConfig entity) {
        boolean result = super.save(entity);
        clearConfigCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysConfig entity) {
        boolean result = super.updateById(entity);
        clearConfigCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        clearConfigCache();
        return result;
    }

    @Override
    public void clearConfigCache() {
        redissonClient.getKeys().deleteByPattern(CONFIG_KEY_PREFIX + "*");
    }
}