package com.istream.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.message.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息主表 Mapper
 *
 * @author istream
 * @since 2026-09-21
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}