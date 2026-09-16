package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.query.SysDictDataQuery;
import com.istream.system.entity.SysDictData;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.service.SysDictDataService;
import com.istream.framework.cache.CacheService;
import com.istream.framework.util.SqlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.istream.common.constant.Constants.DICT_MAP_KEY;

/**
 * 字典数据服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final CacheService cacheService;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public IPage<SysDictData> page(SysDictDataQuery query) {
        Page<SysDictData> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysDictData>()
                .eq(query.getDictType() != null && !query.getDictType().isEmpty(),
                        SysDictData::getDictType, query.getDictType())
                .like(query.getDictLabel() != null && !query.getDictLabel().isEmpty(),
                        SysDictData::getDictLabel, SqlUtils.escapeLike(query.getDictLabel()))
                .orderByAsc(SysDictData::getOrderNum));
    }

    @Override
    public Map<String, List<SysDictData>> getDictMap() {
        Map<String, List<SysDictData>> cached = cacheService.get(DICT_MAP_KEY);
        if (cached != null) {
            return cached;
        }
        List<SysDictData> list = list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, StatusEnum.ENABLED.getCode())
                .orderByAsc(SysDictData::getOrderNum));
        Map<String, List<SysDictData>> result = list.stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        cacheService.set(DICT_MAP_KEY, result, CACHE_TTL);
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
        cacheService.delete(DICT_MAP_KEY);
    }

    @Override
    public List<SysDictData> listByType(String dictType) {
        return list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, StatusEnum.ENABLED.getCode())
                .orderByAsc(SysDictData::getOrderNum));
    }
}