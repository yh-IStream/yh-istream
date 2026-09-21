package com.istream.message.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.model.R;
import com.istream.message.center.MessageCenter;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 消息中心控制器
 *
 * <p>提供消息未读数查询、全部已读、单条已读等接口。
 * 消息列表通过 SSE 实时推送，前端直接维护本地列表。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Slf4j
@Tag(name = "消息中心")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageCenter messageCenter;

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public R<Map<String, Long>> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        long count = messageCenter.getUnreadCount(userId);
        return R.ok(Map.of("count", count));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{messageId}/read")
    public R<Void> markAsRead(@PathVariable Long messageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        messageCenter.markAsRead(messageId, userId);
        return R.ok();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public R<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        messageCenter.markAllAsRead(userId);
        return R.ok();
    }
}