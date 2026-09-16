package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.model.query.SysLoginInfoQuery;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.mapper.SysLoginInfoMapper;
import com.istream.system.service.SysLoginInfoService;
import com.istream.framework.util.SqlUtils;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
public class SysLoginInfoServiceImpl extends ServiceImpl<SysLoginInfoMapper, SysLoginInfo> implements SysLoginInfoService {

    @Override
    public IPage<SysLoginInfo> page(SysLoginInfoQuery query) {
        Page<SysLoginInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysLoginInfo>()
                .like(query.getUsername() != null && !query.getUsername().isEmpty(),
                        SysLoginInfo::getUsername, SqlUtils.escapeLike(query.getUsername()))
                .like(query.getIpAddress() != null && !query.getIpAddress().isEmpty(),
                        SysLoginInfo::getIpAddress, SqlUtils.escapeLike(query.getIpAddress()))
                .eq(query.getStatus() != null, SysLoginInfo::getStatus, query.getStatus())
                .orderByDesc(SysLoginInfo::getLoginTime));
    }

    @Override
    public void truncate() {
        baseMapper.truncate();
    }

    @Override
    public IPage<SysLoginInfo> pageExport(long pageNum, long pageSize) {
        Page<SysLoginInfo> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysLoginInfo>()
                .orderByDesc(SysLoginInfo::getLoginTime));
    }
}