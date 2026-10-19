package com.istream.message.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.framework.tenant.TenantContext;
import com.istream.message.entity.SysMessage;
import com.istream.message.mapper.SysMessageMapper;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;
import com.istream.message.service.SysMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息主表 Service 实现
 *
 * @author istream
 * @since 2026-09-21
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Override
    @Transactional(readOnly = true)
    public IPage<SysMessageDTO> page(Long userId, SysMessageQuery query) {
        Page<SysMessageDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectUserMessagePage(page, userId, TenantContext.getTenantId(), query);
    }
}