package com.istream.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.ai.entity.AiSuggestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 处置建议 Mapper
 *
 * @author istream
 * @since 2026-09-27
 */
@Mapper
public interface AiSuggestionMapper extends BaseMapper<AiSuggestion> {
}