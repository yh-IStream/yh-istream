package com.istream.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.istream.ai.entity.AiAlertRecipient;
import com.istream.ai.service.AIRecipientService;
import com.istream.common.model.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 告警接收人管理控制器
 *
 * <p>提供告警接收人的增删改查接口，管理员可配置哪些用户接收 AI 异常检测告警。</p>
 *
 * @author istream
 * @since 2026-09-27
 */
@Slf4j
@RestController
@RequestMapping("/ai/recipient")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class AIRecipientController {

    private final AIRecipientService recipientService;

    /**
     * 查询所有告警接收人
     */
    @SaCheckLogin
    @GetMapping("/list")
    public R<List<AiAlertRecipient>> list() {
        return R.ok(recipientService.list());
    }

    /**
     * 添加告警接收人
     */
    @SaCheckLogin
    @PostMapping("/add")
    public R<AiAlertRecipient> add(@RequestBody AiAlertRecipient recipient) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return R.ok(recipientService.add(recipient, currentUserId));
    }

    /**
     * 更新告警接收人（启用/停用）
     */
    @SaCheckLogin
    @PutMapping("/{id}")
    public R<AiAlertRecipient> update(@PathVariable Long id, @RequestBody AiAlertRecipient recipient) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return R.ok(recipientService.update(id, recipient, currentUserId));
    }

    /**
     * 删除告警接收人
     */
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        recipientService.delete(id);
        return R.ok();
    }
}