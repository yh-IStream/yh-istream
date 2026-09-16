package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.model.query.SysConfigQuery;
import com.istream.system.entity.SysConfig;
import com.istream.system.mapper.SysConfigMapper;
import com.istream.system.service.SysConfigService;
import com.istream.framework.cache.CacheService;
import com.istream.framework.util.SqlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;

/**
 * 系统配置服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final CacheService cacheService;

    private static final String CONFIG_KEY_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public IPage<SysConfig> page(SysConfigQuery query) {
        Page<SysConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysConfig>()
                .like(query.getConfigName() != null && !query.getConfigName().isEmpty(),
                        SysConfig::getConfigName, SqlUtils.escapeLike(query.getConfigName()))
                .like(query.getConfigKey() != null && !query.getConfigKey().isEmpty(),
                        SysConfig::getConfigKey, SqlUtils.escapeLike(query.getConfigKey()))
                .orderByDesc(SysConfig::getCreateTime));
    }

    @Override
    public boolean existsByConfigKey(String configKey, Long excludeId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(SysConfig::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public String getConfigValueByKey(String configKey) {
        String cacheKey = CONFIG_KEY_PREFIX + configKey;
        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        String value = config != null ? config.getConfigValue() : null;
        if (value != null) {
            cacheService.set(cacheKey, value, CACHE_TTL);
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
        if (result) {
            clearConfigCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        boolean result = super.removeByIds(list);
        if (result) {
            clearConfigCache();
        }
        return result;
    }

    @Override
    public void clearConfigCache() {
        cacheService.deleteByPattern(CONFIG_KEY_PREFIX + "*");
    }
}