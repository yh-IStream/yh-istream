-- ============================================================
-- 消息中心表（yh-istream-message 模块）
-- ============================================================

-- 14. 消息主表
CREATE TABLE sys_message
(
    id          BIGINT       NOT NULL COMMENT '消息ID',
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID（0=非租户模式）',
    title       VARCHAR(200) NOT NULL COMMENT '消息标题',
    content     TEXT                  DEFAULT NULL COMMENT '消息内容',
    event_type  VARCHAR(50)  NOT NULL DEFAULT 'NOTIFICATION' COMMENT '事件类型（DATA_CHANGE/NOTIFICATION/SYSTEM）',
    entity_type VARCHAR(100)          DEFAULT NULL COMMENT '关联实体类型（如SysOrder，用于聚合）',
    entity_id   BIGINT                DEFAULT NULL COMMENT '关联实体ID（用于聚合）',
    sender_id   BIGINT                DEFAULT NULL COMMENT '发送者ID',
    sender_name VARCHAR(50)           DEFAULT NULL COMMENT '发送者名称',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    PRIMARY KEY (id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_event_type (event_type),
    INDEX idx_entity_aggregate (entity_type, entity_id, event_type),
    INDEX idx_create_time (create_time),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='消息主表';

-- 15. 用户消息关联表
CREATE TABLE sys_message_user
(
    id         BIGINT   NOT NULL COMMENT '主键ID',
    message_id BIGINT   NOT NULL COMMENT '消息ID',
    user_id    BIGINT   NOT NULL COMMENT '接收用户ID',
    is_read    TINYINT  NOT NULL DEFAULT 0 COMMENT '是否已读（0=未读 1=已读）',
    read_time  DATETIME          DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (id),
    INDEX idx_message_id (message_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_unread (user_id, is_read)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户消息关联表';