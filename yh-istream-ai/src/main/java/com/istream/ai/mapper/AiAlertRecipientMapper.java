package com.istream.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.ai.entity.AiAlertRecipient;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 告警接收人 Mapper
 *
 * @author istream
 * @since 2026-09-27
 */
@Mapper
public interface AiAlertRecipientMapper extends BaseMapper<AiAlertRecipient> {
}