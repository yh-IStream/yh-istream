package com.istream.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.message.entity.SysMessageUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户消息关联表 Mapper
 *
 * @author istream
 * @since 2026-09-21
 */
@Mapper
public interface SysMessageUserMapper extends BaseMapper<SysMessageUser> {
}