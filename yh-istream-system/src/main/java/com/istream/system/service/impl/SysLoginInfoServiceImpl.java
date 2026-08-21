package com.istream.system.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.mapper.SysLoginInfoMapper;
import com.istream.system.service.SysLoginInfoService;
import org.springframework.stereotype.Service;

@Service
public class SysLoginInfoServiceImpl extends ServiceImpl<SysLoginInfoMapper, SysLoginInfo> implements SysLoginInfoService {

    @Override
    public void truncate() {
        baseMapper.truncate();
    }
}