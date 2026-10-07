package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.model.query.logininfo.SysLoginInfoQuery;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.mapper.SysLoginInfoMapper;
import com.istream.system.service.SysLoginInfoService;
import com.istream.framework.util.SqlUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
public class SysLoginInfoServiceImpl extends ServiceImpl<SysLoginInfoMapper, SysLoginInfo> implements SysLoginInfoService {

    @Override
    @Transactional(readOnly = true)
    public IPage<SysLoginInfo> page(SysLoginInfoQuery query) {
        Page<SysLoginInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysLoginInfo>()
                .like(query.getUsername() != null && !query.getUsername().isBlank(),
                        SysLoginInfo::getUsername, SqlUtils.escapeLike(query.getUsername()))
                .like(query.getIpAddress() != null && !query.getIpAddress().isBlank(),
                        SysLoginInfo::getIpAddress, SqlUtils.escapeLike(query.getIpAddress()))
                .eq(query.getStatus() != null, SysLoginInfo::getStatus, query.getStatus())
                .orderByDesc(SysLoginInfo::getLoginTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void truncate() {
        baseMapper.truncate();
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SysLoginInfo> pageExport(long pageNum, long pageSize) {
        Page<SysLoginInfo> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysLoginInfo>()
                .orderByDesc(SysLoginInfo::getLoginTime));
    }
}