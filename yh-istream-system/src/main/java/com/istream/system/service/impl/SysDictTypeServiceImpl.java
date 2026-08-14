package com.istream.system.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDictType;
import com.istream.system.mapper.SysDictTypeMapper;
import com.istream.system.service.SysDictTypeService;
import org.springframework.stereotype.Service;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {
}