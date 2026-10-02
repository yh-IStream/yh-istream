package com.istream.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.istream.ai.entity.AiAlertRecipient;
import com.istream.ai.mapper.AiAlertRecipientMapper;
import com.istream.common.model.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    private final AiAlertRecipientMapper recipientMapper;

    /**
     * 查询所有告警接收人
     */
    @SaCheckLogin
    @GetMapping("/list")
    public R<List<AiAlertRecipient>> list() {
        List<AiAlertRecipient> recipients = recipientMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiAlertRecipient>()
                        .orderByDesc(AiAlertRecipient::getCreateTime));
        return R.ok(recipients);
    }

    /**
     * 添加告警接收人
     */
    @SaCheckLogin
    @PostMapping("/add")
    public R<AiAlertRecipient> add(@RequestBody AiAlertRecipient recipient) {
        if (recipient.getUserId() == null) {
            return R.fail("用户ID不能为空");
        }
        if (recipient.getUserName() == null || recipient.getUserName().isBlank()) {
            recipient.setUserName("用户" + recipient.getUserId());
        }
        if (recipient.getEnabled() == null) {
            recipient.setEnabled(1);
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();
        recipient.setCreateBy(currentUserId);
        recipient.setCreateTime(LocalDateTime.now());

        recipientMapper.insert(recipient);
        log.info("AIRecipientController: 添加告警接收人 userId={}, name={}", recipient.getUserId(), recipient.getUserName());
        return R.ok(recipient);
    }

    /**
     * 更新告警接收人（启用/停用）
     */
    @SaCheckLogin
    @PutMapping("/{id}")
    public R<AiAlertRecipient> update(@PathVariable Long id, @RequestBody AiAlertRecipient recipient) {
        AiAlertRecipient existing = recipientMapper.selectById(id);
        if (existing == null) {
            return R.fail("接收人不存在");
        }
        if (recipient.getEnabled() != null) {
            existing.setEnabled(recipient.getEnabled());
        }
        if (recipient.getUserName() != null && !recipient.getUserName().isBlank()) {
            existing.setUserName(recipient.getUserName());
        }
        existing.setUpdateBy(StpUtil.getLoginIdAsLong());
        existing.setUpdateTime(LocalDateTime.now());
        recipientMapper.updateById(existing);
        log.info("AIRecipientController: 更新告警接收人 id={}, enabled={}", id, existing.getEnabled());
        return R.ok(existing);
    }

    /**
     * 删除告警接收人
     */
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        recipientMapper.deleteById(id);
        log.info("AIRecipientController: 删除告警接收人 id={}", id);
        return R.ok();
    }
}