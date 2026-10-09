package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.model.query.dict.SysDictTypeQuery;
import com.istream.system.entity.SysDictData;
import com.istream.system.entity.SysDictType;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.mapper.SysDictTypeMapper;
import com.istream.system.service.SysDictTypeService;
import com.istream.system.converter.SysDictTypeConverter;
import com.istream.system.model.dto.dict.SysDictTypeSaveDTO;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.framework.cache.CacheService;
import com.istream.framework.util.SqlUtils;
import lombok.RequiredArgsConstructor;
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
    private final CacheService cacheService;
    private final SysDictTypeConverter sysDictTypeConverter;

    @Override
    @Transactional(readOnly = true)
    public IPage<SysDictType> page(SysDictTypeQuery query) {
        Page<SysDictType> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysDictType>()
                .like(query.getDictName() != null && !query.getDictName().isBlank(),
                        SysDictType::getDictName, SqlUtils.escapeLike(query.getDictName()))
                .like(query.getDictType() != null && !query.getDictType().isBlank(),
                        SysDictType::getDictType, SqlUtils.escapeLike(query.getDictType()))
                .orderByDesc(SysDictType::getCreateTime));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDictType(String dictType, Long excludeId) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType);
        if (excludeId != null) {
            wrapper.ne(SysDictType::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictType(SysDictTypeSaveDTO dto) {
        if (existsByDictType(dto.getDictType(), null)) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
        SysDictType entity = sysDictTypeConverter.toEntity(dto);
        save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(SysDictTypeSaveDTO dto) {
        if (dto.getDictType() != null && existsByDictType(dto.getDictType(), dto.getId())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
        SysDictType entity = new SysDictType();
        entity.setId(dto.getId());
        sysDictTypeConverter.updateEntity(entity, dto);
        updateById(entity);
    }

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
        cacheService.delete(DICT_MAP_KEY);
    }
}