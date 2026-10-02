package com.istream.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 告警接收人配置实体
 *
 * <p>配置哪些用户接收 AI 异常检测告警。
 * 系统管理员可通过前端页面增删改查接收人。</p>
 *
 * @author istream
 * @since 2026-09-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_alert_recipient")
public class AiAlertRecipient extends BaseEntity {

    /** 接收用户ID */
    private Long userId;

    /** 接收用户名称 */
    private String userName;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;
}