package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDictData;
import com.istream.system.entity.SysDictType;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.mapper.SysDictTypeMapper;
import com.istream.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.istream.common.constant.Constants.DICT_MAP_KEY;

/**
 * 字典类型服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataMapper sysDictDataMapper;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDictType entity) {
        boolean result = super.save(entity);
        clearDictCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDictType entity) {
        SysDictType old = getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result && old != null && !old.getDictType().equals(entity.getDictType())) {
            sysDictDataMapper.update(null, new LambdaUpdateWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, old.getDictType())
                    .set(SysDictData::getDictType, entity.getDictType()));
        }
        clearDictCache();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        SysDictType type = getById(id);
        boolean result = super.removeById(id);
        if (result && type != null) {
            sysDictDataMapper.delete(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, type.getDictType()));
            clearDictCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        @SuppressWarnings("unchecked")
        List<SysDictType> types = listByIds((Collection<? extends Serializable>) list);
        boolean result = super.removeByIds(list);
        if (result) {
            List<String> dictTypes = types.stream()
                    .map(SysDictType::getDictType)
                    .toList();
            if (!dictTypes.isEmpty()) {
                sysDictDataMapper.delete(new LambdaQueryWrapper<SysDictData>()
                        .in(SysDictData::getDictType, dictTypes));
            }
            clearDictCache();
        }
        return result;
    }

    private void clearDictCache() {
        redissonClient.getBucket(DICT_MAP_KEY).delete();
    }
}