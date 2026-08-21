package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDictData;
import com.istream.system.entity.SysDictType;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.mapper.SysDictTypeMapper;
import com.istream.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataMapper sysDictDataMapper;

    @Override
    public boolean hasDictData(Long dictTypeId) {
        SysDictType dictType = getById(dictTypeId);
        if (dictType == null) {
            return false;
        }
        return sysDictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType.getDictType())) > 0;
    }

    @Override
    public boolean hasDictDataAny(List<Long> dictTypeIds) {
        if (dictTypeIds == null || dictTypeIds.isEmpty()) {
            return false;
        }
//        List<SysDictType> dictTypes = listByIds(dictTypeIds);
        List<SysDictType> dictTypes = getBaseMapper().selectList(
                new LambdaQueryWrapper<SysDictType>().in(SysDictType::getId, dictTypeIds));
        List<String> typeCodes = dictTypes.stream()
                .map(SysDictType::getDictType)
                .distinct()
                .toList();
        if (typeCodes.isEmpty()) {
            return false;
        }
        return sysDictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .in(SysDictData::getDictType, typeCodes)) > 0;
    }
}