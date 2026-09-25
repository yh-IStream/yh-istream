package com.istream.watchdog.listener;

import com.istream.watchdog.model.AlertRule;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警规则提供者
 *
 * <p>管理告警规则的注册和查询。当前为内存实现，
 * 后续可扩展为数据库持久化 + YAML 配置加载。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Component
public class RuleProvider {

    private final Map<String, List<AlertRule>> rulesByEntity = new ConcurrentHashMap<>();

    /**
     * 注册规则
     *
     * @param rule 告警规则
     */
    public void register(AlertRule rule) {
        rulesByEntity.computeIfAbsent(rule.getEntity(), k ->
                Collections.synchronizedList(new java.util.ArrayList<>())).add(rule);
    }

    /**
     * 批量注册规则
     *
     * @param rules 告警规则列表
     */
    public void registerAll(List<AlertRule> rules) {
        for (AlertRule rule : rules) {
            register(rule);
        }
    }

    /**
     * 查询实体类型对应的规则
     *
     * @param entityType 实体类型名称
     * @return 匹配的规则列表
     */
    public List<AlertRule> getRules(String entityType) {
        return rulesByEntity.getOrDefault(entityType, Collections.emptyList());
    }

    /**
     * 移除规则
     *
     * @param ruleName 规则名称
     */
    public void remove(String ruleName) {
        rulesByEntity.values().forEach(rules ->
                rules.removeIf(rule -> ruleName.equals(rule.getName())));
    }

    /**
     * 清空所有规则
     */
    public void clear() {
        rulesByEntity.clear();
    }
}