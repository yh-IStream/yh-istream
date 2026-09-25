package com.istream.message.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.model.R;
import com.istream.message.center.MessageCenter;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;
import com.istream.message.service.SysMessageService;
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
 * <p>提供消息分页列表、未读数查询、全部已读、单条已读等接口。
 * 新消息通过 SSE 实时推送，前端维护本地列表和未读计数。</p>
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
    private final SysMessageService sysMessageService;

    @Operation(summary = "分页查询消息列表")
    @GetMapping("/list")
    public R<IPage<SysMessageDTO>> list(SysMessageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(sysMessageService.page(userId, query));
    }

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