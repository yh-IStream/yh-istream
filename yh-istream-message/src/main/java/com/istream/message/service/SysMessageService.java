package com.istream.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.message.entity.SysMessage;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;

/**
 * 消息主表 Service 接口
 *
 * @author istream
 * @since 2026-09-21
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 分页查询用户消息（关联 sys_message_user 获取已读状态）
     *
     * @param userId 用户ID
     * @param query  查询参数
     * @return 分页消息DTO
     */
    IPage<SysMessageDTO> page(Long userId, SysMessageQuery query);
}