-- ============================================================
-- AI 处置建议 & 告警接收人表（yh-istream-ai 模块）
-- ============================================================

-- 1. AI 处置建议表
CREATE TABLE sys_ai_suggestion
(
    id              BIGINT        NOT NULL COMMENT '主键ID',
    alert_id        BIGINT        DEFAULT NULL COMMENT '关联告警ID',
    entity_type     VARCHAR(100)  NOT NULL COMMENT '实体类型',
    entity_id       BIGINT        NOT NULL COMMENT '实体ID',
    action          VARCHAR(100)  NOT NULL COMMENT 'AI建议的操作（如freezeEntity）',
    params          JSON          DEFAULT NULL COMMENT '操作参数（JSON格式）',
    confidence      DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT 'AI置信度（0.00~1.00）',
    conclusion      TEXT          DEFAULT NULL COMMENT 'AI分析结论',
    suggest_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建议生成时间',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态（0=待确认 1=已执行 2=已拒绝）',
    confirmed_by    BIGINT        DEFAULT NULL COMMENT '确认人ID',
    confirmed_time  DATETIME      DEFAULT NULL COMMENT '确认时间',
    tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID（0=非租户模式）',
    create_by       BIGINT        DEFAULT NULL COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT        DEFAULT NULL COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0=正常 1=删除）',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_suggest_time (suggest_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI处置建议表';

-- 2. AI 告警接收人配置表
CREATE TABLE sys_ai_alert_recipient
(
    id          BIGINT  NOT NULL COMMENT '主键ID',
    user_id     BIGINT  NOT NULL COMMENT '接收用户ID',
    user_name   VARCHAR(50)  DEFAULT NULL COMMENT '接收用户名称',
    tenant_id   BIGINT  NOT NULL DEFAULT 0 COMMENT '租户ID（0=非租户模式）',
    enabled     TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
    create_by   BIGINT  DEFAULT NULL COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT  DEFAULT NULL COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0=正常 1=删除）',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_tenant (user_id, tenant_id),
    INDEX idx_enabled (enabled)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI告警接收人配置表';