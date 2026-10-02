package com.istream.watchdog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 告警规则
 *
 * <p>YAML 声明的告警规则，由 {@link RuleEngine} 评估。
 * 每条规则绑定一个实体类型，当该实体的数据变更事件到达时触发评估。</p>
 *
 * <p>规则配置示例（YAML）：</p>
 * <pre>{@code
 * istream:
 *   watchdog:
 *     rules:
 *       - name: large-order-alert
 *         entity: SysOrder
 *         on-events: [CREATE, UPDATE]
 *         condition: "#data.amount > 10000"
 *         severity: 3
 *         title: "大额订单告警"
 *         content: "订单金额 {{amount}} 超过阈值 10000"
 *         receivers: [1, 2]
 *         channels: [wecom, email]
 * }</pre>
 *
 * @author istream
 * @since 2026-09-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    /** 规则名称（唯一标识） */
    private String name;

    /** 绑定的实体类型（如 "SysOrder"） */
    private String entity;

    /** 触发评估的变更事件类型 */
    private List<String> onEvents;

    /** SpEL 条件表达式（如 "#data.amount > 10000"） */
    private String condition;

    /** 告警严重级别（1=低, 2=中, 3=高, 4=紧急） */
    private int severity;

    /** 告警标题模板（支持 {{变量}} 占位符） */
    private String title;

    /** 告警内容模板（支持 {{变量}} 占位符） */
    private String content;

    /** 默认接收人用户ID列表 */
    private List<Long> receivers;

    /** 默认推送通道 */
    private List<String> channels;

    /** 规则是否启用 */
    private boolean enabled;
}