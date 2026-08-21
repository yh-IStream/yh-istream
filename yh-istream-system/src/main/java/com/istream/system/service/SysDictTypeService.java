package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysDictType;

import java.util.List;

public interface SysDictTypeService extends IService<SysDictType> {

    boolean hasDictData(Long dictTypeId);

    boolean hasDictDataAny(List<Long> dictTypeIds);
}