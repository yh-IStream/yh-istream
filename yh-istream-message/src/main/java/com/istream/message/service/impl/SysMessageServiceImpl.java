package com.istream.message.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.message.entity.SysMessage;
import com.istream.message.mapper.SysMessageMapper;
import com.istream.message.service.SysMessageService;
import org.springframework.stereotype.Service;

/**
 * 消息主表 Service 实现
 *
 * @author istream
 * @since 2026-09-21
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {
}