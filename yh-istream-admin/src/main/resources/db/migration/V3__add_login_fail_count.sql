-- =============================================
-- V3__add_login_fail_count.sql
-- 添加登录失败次数字段，MySQL 9.7.2 LTS 持久化锁定状态
-- =============================================

ALTER TABLE sys_user
    ADD COLUMN login_fail_count INT NOT NULL DEFAULT 0 COMMENT '登录失败次数'
    AFTER login_count;

UPDATE sys_user SET login_fail_count = 0 WHERE login_fail_count IS NULL;