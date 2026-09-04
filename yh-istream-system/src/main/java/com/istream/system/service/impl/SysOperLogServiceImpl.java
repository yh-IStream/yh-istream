package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysOperLog;
import com.istream.system.mapper.SysOperLogMapper;
import com.istream.system.service.SysOperLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public void truncate() {
        baseMapper.truncate();
    }

    @Override
    public long countTodayOps(LocalDateTime todayStart) {
        return count(new LambdaQueryWrapper<SysOperLog>()
                .ge(SysOperLog::getOperTime, todayStart));
    }
}