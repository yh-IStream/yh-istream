package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.model.query.operlog.SysOperLogQuery;
import com.istream.system.entity.SysOperLog;
import com.istream.system.mapper.SysOperLogMapper;
import com.istream.system.service.SysOperLogService;
import com.istream.framework.util.SqlUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    @Transactional(readOnly = true)
    public IPage<SysOperLog> page(SysOperLogQuery query) {
        Page<SysOperLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysOperLog>()
                .like(query.getTitle() != null && !query.getTitle().isEmpty(),
                        SysOperLog::getTitle, SqlUtils.escapeLike(query.getTitle()))
                .eq(query.getBusinessType() != null, SysOperLog::getBusinessType, query.getBusinessType())
                .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                .orderByDesc(SysOperLog::getOperTime));
    }

    @Override
    public void truncate() {
        baseMapper.truncate();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodayOps(LocalDateTime todayStart) {
        return count(new LambdaQueryWrapper<SysOperLog>()
                .ge(SysOperLog::getOperTime, todayStart));
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SysOperLog> pageExport(long pageNum, long pageSize) {
        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysOperLog>()
                .orderByDesc(SysOperLog::getOperTime));
    }
}