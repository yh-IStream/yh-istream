package com.istream.message.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.message.entity.SysMessageUser;
import com.istream.message.mapper.SysMessageUserMapper;
import com.istream.message.service.SysMessageUserService;
import org.springframework.stereotype.Service;

/**
 * 用户消息关联表 Service 实现
 *
 * @author istream
 * @since 2026-09-21
 */
@Service
public class SysMessageUserServiceImpl extends ServiceImpl<SysMessageUserMapper, SysMessageUser> implements SysMessageUserService {
}