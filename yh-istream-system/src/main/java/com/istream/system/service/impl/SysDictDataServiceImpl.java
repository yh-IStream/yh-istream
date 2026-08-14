package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDictData;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.service.SysDictDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public Map<String, List<SysDictData>> getDictMap() {
        List<SysDictData> list = list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getOrderNum));
        return list.stream().collect(Collectors.groupingBy(SysDictData::getDictType));
    }
}