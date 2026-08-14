package com.istream.system.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysConfig;
import com.istream.system.mapper.SysConfigMapper;
import com.istream.system.service.SysConfigService;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
}