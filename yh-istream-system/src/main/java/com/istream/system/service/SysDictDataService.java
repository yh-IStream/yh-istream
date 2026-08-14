package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysDictData;

import java.util.List;
import java.util.Map;

public interface SysDictDataService extends IService<SysDictData> {

    Map<String, List<SysDictData>> getDictMap();
}