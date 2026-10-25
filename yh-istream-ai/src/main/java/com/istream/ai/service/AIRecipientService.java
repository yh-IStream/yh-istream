package com.istream.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.istream.ai.entity.AiAlertRecipient;
import com.istream.ai.mapper.AiAlertRecipientMapper;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 告警接收人服务
 *
 * @author istream
 * @since 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class AIRecipientService {

    private final AiAlertRecipientMapper recipientMapper;

    /**
     * 查询所有告警接收人
     */
    public List<AiAlertRecipient> list() {
        return recipientMapper.selectList(
                new LambdaQueryWrapper<AiAlertRecipient>()
                        .orderByDesc(AiAlertRecipient::getCreateTime));
    }

    /**
     * 添加告警接收人
     *
     * @param recipient   接收人信息
     * @param currentUserId 当前操作人ID
     */
    public AiAlertRecipient add(AiAlertRecipient recipient, Long currentUserId) {
        if (recipient.getUserId() == null) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "用户ID不能为空");
        }

        Long existingCount = recipientMapper.selectCount(
                new LambdaQueryWrapper<AiAlertRecipient>()
                        .eq(AiAlertRecipient::getUserId, recipient.getUserId()));
        if (existingCount > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "该用户已是告警接收人");
        }

        if (recipient.getUserName() == null || recipient.getUserName().isBlank()) {
            recipient.setUserName("用户" + recipient.getUserId());
        }
        if (recipient.getEnabled() == null) {
            recipient.setEnabled(true);
        }

        recipient.setCreateBy(currentUserId);
        recipient.setCreateTime(LocalDateTime.now());
        recipientMapper.insert(recipient);

        log.info("AIRecipientService: 添加告警接收人 userId={}, name={}", recipient.getUserId(), recipient.getUserName());
        return recipient;
    }

    /**
     * 更新告警接收人
     *
     * @param id          接收人ID
     * @param recipient   更新内容
     * @param currentUserId 当前操作人ID
     */
    public AiAlertRecipient update(Long id, AiAlertRecipient recipient, Long currentUserId) {
        AiAlertRecipient existing = recipientMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "接收人不存在");
        }

        LambdaUpdateWrapper<AiAlertRecipient> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiAlertRecipient::getId, id);

        if (recipient.getEnabled() != null) {
            updateWrapper.set(AiAlertRecipient::getEnabled, recipient.getEnabled());
        }
        if (recipient.getUserName() != null && !recipient.getUserName().isBlank()) {
            updateWrapper.set(AiAlertRecipient::getUserName, recipient.getUserName());
        }
        updateWrapper.set(AiAlertRecipient::getUpdateBy, currentUserId);
        updateWrapper.set(AiAlertRecipient::getUpdateTime, LocalDateTime.now());

        recipientMapper.update(null, updateWrapper);

        log.info("AIRecipientService: 更新告警接收人 id={}, enabled={}", id, recipient.getEnabled());
        return recipientMapper.selectById(id);
    }

    /**
     * 删除告警接收人
     *
     * @param id 接收人ID
     */
    public void delete(Long id) {
        AiAlertRecipient existing = recipientMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "接收人不存在");
        }
        recipientMapper.deleteById(id);
        log.info("AIRecipientService: 删除告警接收人 id={}", id);
    }
}