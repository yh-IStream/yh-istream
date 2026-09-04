package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDictData;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.service.SysDictDataService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.istream.common.constant.Constants.DICT_MAP_KEY;

@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final RedissonClient redissonClient;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Map<String, List<SysDictData>> getDictMap() {
        RBucket<Map<String, List<SysDictData>>> bucket = redissonClient.getBucket(DICT_MAP_KEY);
        Map<String, List<SysDictData>> cached = bucket.get();
        if (cached != null) {
            return cached;
        }
        List<SysDictData> list = list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getOrderNum));
        Map<String, List<SysDictData>> result = list.stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        bucket.set(result, CACHE_TTL);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDictData entity) {
        boolean result = super.save(entity);
        clearDictCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDictData entity) {
        boolean result = super.updateById(entity);
        clearDictCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        clearDictCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        boolean result = super.removeByIds(list);
        if (result) {
            clearDictCache();
        }
        return result;
    }

    private void clearDictCache() {
        redissonClient.getBucket(DICT_MAP_KEY).delete();
    }
}