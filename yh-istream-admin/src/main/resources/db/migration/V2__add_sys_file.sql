-- =============================================
-- 文件管理表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`                 BIGINT        NOT NULL COMMENT '主键ID',
    `file_name`          VARCHAR(255)  NOT NULL COMMENT '存储文件名',
    `original_name`      VARCHAR(255)  NOT NULL COMMENT '原始文件名',
    `file_path`          VARCHAR(500)  NOT NULL COMMENT '文件存储路径',
    `file_size`          BIGINT        NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `mime_type`          VARCHAR(128)  DEFAULT NULL COMMENT 'MIME类型',
    `file_ext`           VARCHAR(32)   DEFAULT NULL COMMENT '文件扩展名',
    `storage_type`       VARCHAR(32)   NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型（LOCAL/MINIO/ALIYUN_OSS/TENCENT_COS）',
    `storage_url`        VARCHAR(500)  DEFAULT NULL COMMENT '访问URL',
    `module`             VARCHAR(64)   DEFAULT 'common' COMMENT '所属模块',
    `create_by`          BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           TINYINT       NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    `remark`             VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件管理表';

-- 文件管理权限
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `menu_type`, `icon`, `order_num`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) VALUES
(110, 0,   '文件管理', 'file',   NULL,     NULL,                      'M', 'upload',  6, 0, 1, NOW(), 1, NOW(), 0),
(111, 110, '文件列表', 'list',   'file/list',   'system:file:list',    'C', 'list',    1, 0, 1, NOW(), 1, NOW(), 0),
(112, 110, '文件上传', NULL,     NULL,          'system:file:upload',  'F', NULL,      2, 0, 1, NOW(), 1, NOW(), 0),
(113, 110, '文件下载', NULL,     NULL,          'system:file:download','F', NULL,      3, 0, 1, NOW(), 1, NOW(), 0),
(114, 110, '文件删除', NULL,     NULL,          'system:file:delete',  'F', NULL,      4, 0, 1, NOW(), 1, NOW(), 0);