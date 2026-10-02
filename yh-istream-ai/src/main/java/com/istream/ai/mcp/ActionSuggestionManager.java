package com.istream.ai.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.ai.entity.AiSuggestion;
import com.istream.ai.mapper.AiSuggestionMapper;
import com.istream.ai.model.ActionSuggestion;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处置建议管理器（MySQL 持久化）
 *
 * <p>管理 AI 生成的处置建议，提供创建、查询、确认、拒绝操作。
 * 所有处置操作必须经过人工确认后才执行（安全红线）。
 * 数据持久化到 sys_ai_suggestion 表，重启不丢失。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
public class ActionSuggestionManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AiSuggestionMapper mapper;

    public ActionSuggestionManager(AiSuggestionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 创建处置建议
     *
     * @param alertId    关联告警ID
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param action     建议操作
     * @param params     操作参数
     * @param confidence 置信度
     * @param conclusion 分析结论
     * @param tenantId   租户ID
     * @return 处置建议 DTO
     */
    public ActionSuggestion create(Long alertId, String entityType, Long entityId,
                                   String action, Map<String, Object> params,
                                   double confidence, String conclusion, Long tenantId) {
        AiSuggestion entity = new AiSuggestion();
        entity.setAlertId(alertId);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setAction(action);
        entity.setParams(toJson(params));
        entity.setConfidence(BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP));
        entity.setConclusion(conclusion);
        entity.setSuggestTime(LocalDateTime.now());
        entity.setStatus(0);
        entity.setTenantId(tenantId);

        mapper.insert(entity);
        log.info("ActionSuggestionManager: 创建处置建议 id={}, action={}, entityType={}/{}",
                entity.getId(), action, entityType, entityId);
        return toDto(entity);
    }

    /**
     * 查询处置建议
     */
    public ActionSuggestion get(Long id) {
        AiSuggestion entity = mapper.selectById(id);
        return entity != null ? toDto(entity) : null;
    }

    /**
     * 查询所有处置建议
     */
    public List<ActionSuggestion> getAll() {
        List<AiSuggestion> entities = mapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiSuggestion>()
                        .orderByDesc(AiSuggestion::getSuggestTime));
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 人工确认执行处置建议
     */
    public ActionSuggestion confirm(Long id, Long confirmedBy) {
        AiSuggestion entity = mapper.selectById(id);
        if (entity == null) {
            log.warn("ActionSuggestionManager: 处置建议不存在 id={}", id);
            return null;
        }
        if (entity.getStatus() != null && entity.getStatus() != 0) {
            log.warn("ActionSuggestionManager: 处置建议已处理 id={}, status={}", id, entity.getStatus());
            return toDto(entity);
        }
        entity.setStatus(1);
        entity.setConfirmedBy(confirmedBy);
        entity.setConfirmedTime(LocalDateTime.now());
        mapper.updateById(entity);
        log.info("ActionSuggestionManager: 处置建议已确认 id={}, action={}, confirmedBy={}",
                id, entity.getAction(), confirmedBy);
        return toDto(entity);
    }

    /**
     * 拒绝处置建议
     */
    public ActionSuggestion reject(Long id, Long confirmedBy) {
        AiSuggestion entity = mapper.selectById(id);
        if (entity == null) {
            log.warn("ActionSuggestionManager: 处置建议不存在 id={}", id);
            return null;
        }
        entity.setStatus(2);
        entity.setConfirmedBy(confirmedBy);
        entity.setConfirmedTime(LocalDateTime.now());
        mapper.updateById(entity);
        log.info("ActionSuggestionManager: 处置建议已拒绝 id={}, action={}, rejectedBy={}",
                id, entity.getAction(), confirmedBy);
        return toDto(entity);
    }

    private ActionSuggestion toDto(AiSuggestion entity) {
        return ActionSuggestion.builder()
                .id(entity.getId())
                .alertId(entity.getAlertId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .action(entity.getAction())
                .params(fromJson(entity.getParams()))
                .confidence(entity.getConfidence() != null ? entity.getConfidence().doubleValue() : 0)
                .conclusion(entity.getConclusion())
                .suggestTime(entity.getSuggestTime())
                .status(entity.getStatus() != null ? entity.getStatus() : 0)
                .confirmedBy(entity.getConfirmedBy())
                .confirmedTime(entity.getConfirmedTime())
                .tenantId(entity.getTenantId())
                .build();
    }

    private String toJson(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.warn("ActionSuggestionManager: 参数序列化失败", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("ActionSuggestionManager: 参数反序列化失败 json={}", json, e);
            return Collections.emptyMap();
        }
    }
}