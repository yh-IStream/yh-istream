package com.istream.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.ai.entity.AiSuggestion;
import com.istream.ai.mapper.AiSuggestionMapper;
import com.istream.ai.model.ActionSuggestion;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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

    /** 处置建议状态：待确认 */
    public static final int STATUS_PENDING = 0;

    /** 处置建议状态：已执行 */
    public static final int STATUS_EXECUTED = 1;

    /** 处置建议状态：已拒绝 */
    public static final int STATUS_REJECTED = 2;

    /** 待确认建议单次查询上限 */
    private static final int PENDING_QUERY_LIMIT = 100;

    private final AiSuggestionMapper mapper;
    private final ObjectMapper objectMapper;

    public ActionSuggestionManager(AiSuggestionMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
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
        entity.setStatus(STATUS_PENDING);
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
     * 查询待确认的处置建议
     *
     * @return 待确认建议列表，无数据时返回空集合
     */
    public List<ActionSuggestion> listPending() {
        Page<AiSuggestion> page = new Page<>(1, PENDING_QUERY_LIMIT, false);
        List<AiSuggestion> entities = mapper.selectPage(page,
                new LambdaQueryWrapper<AiSuggestion>()
                        .eq(AiSuggestion::getStatus, STATUS_PENDING)
                        .orderByDesc(AiSuggestion::getSuggestTime)).getRecords();
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 人工确认执行处置建议
     *
     * <p>采用条件更新（CAS）保证幂等与并发安全</p>
     *
     * @param id          处置建议 ID
     * @param confirmedBy 确认人 ID
     * @return 确认后的处置建议；不存在、已处理或并发抢占失败时返回 null
     */
    public ActionSuggestion confirm(Long id, Long confirmedBy) {
        AiSuggestion patch = new AiSuggestion();
        patch.setStatus(STATUS_EXECUTED);
        patch.setConfirmedBy(confirmedBy);
        patch.setConfirmedTime(LocalDateTime.now());

        int rows = mapper.update(patch, new LambdaUpdateWrapper<AiSuggestion>()
                .eq(AiSuggestion::getId, id)
                .eq(AiSuggestion::getStatus, STATUS_PENDING));
        if (rows == 0) {
            log.warn("ActionSuggestionManager: 处置建议不存在或已处理，拒绝确认 id={}, confirmedBy={}", id, confirmedBy);
            return null;
        }
        log.info("ActionSuggestionManager: 处置建议已确认 id={}, confirmedBy={}", id, confirmedBy);
        return get(id);
    }

    /**
     * 拒绝处置建议
     *
     * <p>与 {@link #confirm} 采用相同的条件更新策略</p>
     *
     * @param id          处置建议 ID
     * @param rejectedBy  拒绝人 ID
     * @return 拒绝后的处置建议；不存在、已处理或并发抢占失败时返回 null
     */
    public ActionSuggestion reject(Long id, Long rejectedBy) {
        AiSuggestion patch = new AiSuggestion();
        patch.setStatus(STATUS_REJECTED);
        patch.setConfirmedBy(rejectedBy);
        patch.setConfirmedTime(LocalDateTime.now());

        int rows = mapper.update(patch, new LambdaUpdateWrapper<AiSuggestion>()
                .eq(AiSuggestion::getId, id)
                .eq(AiSuggestion::getStatus, STATUS_PENDING));
        if (rows == 0) {
            log.warn("ActionSuggestionManager: 处置建议不存在或已处理，无法拒绝 id={}, rejectedBy={}", id, rejectedBy);
            return null;
        }
        log.info("ActionSuggestionManager: 处置建议已拒绝 id={}, rejectedBy={}", id, rejectedBy);
        return get(id);
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
                .status(entity.getStatus() != null ? entity.getStatus() : STATUS_PENDING)
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
        } catch (JacksonException e) {
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
        } catch (JacksonException e) {
            log.warn("ActionSuggestionManager: 参数反序列化失败 json={}", json, e);
            return Collections.emptyMap();
        }
    }
}