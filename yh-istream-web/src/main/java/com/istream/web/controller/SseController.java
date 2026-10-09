package com.istream.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.annotation.RateLimit;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.framework.sse.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * SSE 实时推送控制器
 * <p>
 * 支持操作日志、登录日志等实时推送。客户端通过 EventSource 连接，
 * 每30秒自动心跳保活，连接超时 5 分钟。
 * <p>
 * 安全机制：客户端先用主 Token 调用 /sse/ticket 获取一次性短凭证，
 * 再用 ticket 连接 /sse/subscribe，避免主 Token 暴露在 URL 中。
 *
 * @author istream
 * @since 2026-08-20
 */
@Slf4j
@Tag(name = "实时推送")
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @SaCheckLogin
    @RateLimit(key = "sse:ticket:gen", rate = 5, timeout = 0)
    @Operation(summary = "获取 SSE 一次性连接凭证")
    @GetMapping("/ticket")
    public R<Map<String, String>> ticket() {
        Long userId = StpUtil.getLoginIdAsLong();
        String ticket = sseService.generateTicket(userId);
        return R.ok(Map.of("ticket", ticket));
    }

    @RateLimit(key = "sse:subscribe", rate = 5, timeout = 0)
    @Operation(summary = "订阅实时推送")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam(required = false) String ticket,
            @RequestParam(required = false) String token) {
        if (ticket != null && !ticket.isBlank()) {
            Long userId = sseService.consumeTicket(ticket);
            if (userId == null) {
                log.warn("SSE 订阅失败: ticket 无效或已过期");
                return createErrorEmitter();
            }
            log.info("SSE 订阅(ticket): userId={}", userId);
            return sseService.subscribe(userId);
        }

        if (token != null && !token.isBlank()) {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("SSE 订阅失败: token 无效");
                return createErrorEmitter();
            }
            Long userId = Long.parseLong(String.valueOf(loginId));
            log.info("SSE 订阅: userId={}", userId);
            return sseService.subscribe(userId);
        }

        try {
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("SSE 订阅: userId={}", userId);
            return sseService.subscribe(userId);
        } catch (Exception e) {
            log.warn("SSE 订阅失败: 未登录", e);
            return createErrorEmitter();
        }
    }

    private SseEmitter createErrorEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.completeWithError(new BusinessException(ResultCode.UNAUTHORIZED));
        return emitter;
    }
}