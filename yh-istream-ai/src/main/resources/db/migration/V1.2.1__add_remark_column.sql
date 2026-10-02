ALTER TABLE sys_ai_suggestion
    ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER del_flag;

ALTER TABLE sys_ai_alert_recipient
    ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER del_flag;