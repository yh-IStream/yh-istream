package com.istream.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.istream.ai.config.AIProperties;
import com.istream.ai.mcp.ActionSuggestionManager;
import com.istream.ai.mcp.McpToolRegistry;
import com.istream.ai.model.ActionSuggestion;
import com.istream.common.model.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 处置建议控制器
 *
 * <p>提供前端查询 AI 建议卡片、确认/拒绝处置建议的接口。
 * 仅当 {@code istream.ai.enabled=true} 时注册。</p>
 *
 * @author istream
 * @since 2026-09-25
 */
@Slf4j
@RestController
@RequestMapping("/ai/suggestion")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class AISuggestionController {

    private final ActionSuggestionManager suggestionManager;
    private final McpToolRegistry mcpToolRegistry;
    private final AIProperties properties;

    /**
     * 查询待确认的处置建议
     */
    @SaCheckLogin
    @GetMapping("/pending")
    public R<List<ActionSuggestion>> pending() {
        List<ActionSuggestion> all = suggestionManager.getAll().stream()
                .filter(s -> s.getStatus() == 0)
                .collect(Collectors.toList());
        return R.ok(all);
    }

    /**
     * 查询指定建议详情
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<ActionSuggestion> detail(@PathVariable Long id) {
        ActionSuggestion suggestion = suggestionManager.get(id);
        if (suggestion == null) {
            return R.fail("处置建议不存在");
        }
        return R.ok(suggestion);
    }

    /**
     * 人工确认执行处置建议
     *
     * <p>安全红线：AI 建议不自动执行，必须人工确认。</p>
     */
    @SaCheckLogin
    @PostMapping("/{id}/confirm")
    public R<Object> confirm(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Object result = mcpToolRegistry.executeConfirmed(id, userId);
        return R.ok(result);
    }

    /**
     * 拒绝处置建议
     */
    @SaCheckLogin
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        suggestionManager.reject(id, userId);
        return R.ok();
    }

    /**
     * 查询所有 MCP 工具定义
     */
    @SaCheckLogin
    @GetMapping("/tools")
    public R<List<McpToolRegistry.ToolInfo>> tools() {
        return R.ok(mcpToolRegistry.getToolDefinitions());
    }
}