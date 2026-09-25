package com.istream.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.message.entity.SysMessage;
import com.istream.message.entity.SysMessageUser;
import com.istream.message.mapper.SysMessageMapper;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;
import com.istream.message.service.SysMessageService;
import com.istream.message.service.SysMessageUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息主表 Service 实现
 *
 * @author istream
 * @since 2026-09-21
 */
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    private final SysMessageUserService sysMessageUserService;

    @Override
    public IPage<SysMessageDTO> page(Long userId, SysMessageQuery query) {
        Page<SysMessageUser> muPage = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysMessageUser> muWrapper = new LambdaQueryWrapper<>();
        muWrapper.eq(SysMessageUser::getUserId, userId);
        if (Boolean.TRUE.equals(query.getUnreadOnly())) {
            muWrapper.eq(SysMessageUser::getIsRead, 0);
        }

        Set<Long> eventTypeFilteredIds = null;
        if (query.getEventType() != null && !query.getEventType().isEmpty()) {
            LambdaQueryWrapper<SysMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(SysMessage::getEventType, query.getEventType());
            msgWrapper.select(SysMessage::getId);
            eventTypeFilteredIds = new HashSet<>(listObjs(msgWrapper, obj -> (Long) obj));
            if (eventTypeFilteredIds.isEmpty()) {
                return new Page<>(query.getPageNum(), query.getPageSize(), 0);
            }
            muWrapper.in(SysMessageUser::getMessageId, eventTypeFilteredIds);
        }

        muWrapper.orderByDesc(SysMessageUser::getId);
        Page<SysMessageUser> muResult = sysMessageUserService.page(muPage, muWrapper);

        List<Long> messageIds = muResult.getRecords().stream()
                .map(SysMessageUser::getMessageId)
                .collect(Collectors.toList());
        Map<Long, SysMessageUser> muMap = muResult.getRecords().stream()
                .collect(Collectors.toMap(SysMessageUser::getMessageId, Function.identity(), (a, b) -> a));

        Map<Long, SysMessage> msgMap = listByIds(messageIds).stream()
                .collect(Collectors.toMap(SysMessage::getId, Function.identity()));

        List<SysMessageDTO> dtoList = messageIds.stream()
                .map(id -> {
                    SysMessage msg = msgMap.get(id);
                    SysMessageUser mu = muMap.get(id);
                    if (msg == null) {
                        return null;
                    }
                    SysMessageDTO dto = new SysMessageDTO();
                    dto.setId(msg.getId());
                    dto.setTitle(msg.getTitle());
                    dto.setContent(msg.getContent());
                    dto.setEventType(msg.getEventType());
                    dto.setEntityType(msg.getEntityType());
                    dto.setEntityId(msg.getEntityId());
                    dto.setSenderId(msg.getSenderId());
                    dto.setSenderName(msg.getSenderName());
                    dto.setCreateTime(msg.getCreateTime());
                    dto.setIsRead(mu != null ? mu.getIsRead() : 0);
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<SysMessageDTO> dtoPage = new Page<>(muResult.getCurrent(), muResult.getSize(), muResult.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}