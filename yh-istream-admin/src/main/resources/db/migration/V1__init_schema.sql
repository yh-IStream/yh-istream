-- ============================================================
-- V1__init_schema.sql
-- 初始化 RBAC 核心表结构
-- 数据库: MySQL 9.7.2 LTS / PostgreSQL 16+
-- ============================================================

-- ----------------------------
-- 1. 部门表
-- ----------------------------
CREATE TABLE sys_dept
(
    id          BIGINT       NOT NULL COMMENT '部门ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID',
    ancestors   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '祖级列表',
    dept_name   VARCHAR(100) NOT NULL COMMENT '部门名称',
    order_num   INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    leader      VARCHAR(50)           DEFAULT NULL COMMENT '负责人',
    phone       VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
    email       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='部门表';

-- ----------------------------
-- 2. 用户表
-- ----------------------------
CREATE TABLE sys_user
(
    id          BIGINT       NOT NULL COMMENT '用户ID',
    dept_id     BIGINT                DEFAULT NULL COMMENT '部门ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    email       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    gender      TINYINT               DEFAULT 0 COMMENT '性别（0=未知 1=男 2=女）',
    avatar      VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    login_ip    VARCHAR(128)          DEFAULT NULL COMMENT '最后登录IP',
    login_date  DATETIME              DEFAULT NULL COMMENT '最后登录时间',
    login_count INT          NOT NULL DEFAULT 0 COMMENT '登录次数',
    pwd_reset_time DATETIME           DEFAULT NULL COMMENT '上次密码重置时间',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_username (username),
    INDEX idx_dept_id (dept_id),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- ----------------------------
-- 3. 角色表
-- ----------------------------
CREATE TABLE sys_role
(
    id          BIGINT       NOT NULL COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(50)  NOT NULL COMMENT '角色标识（如admin、user）',
    role_sort   INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    data_scope  TINYINT      NOT NULL DEFAULT 5 COMMENT '数据范围（1=全部 2=自定义 3=本部门 4=本部门及以下 5=仅本人）',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_role_key (role_key),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- ----------------------------
-- 4. 菜单/权限表
-- ----------------------------
CREATE TABLE sys_menu
(
    id          BIGINT       NOT NULL COMMENT '菜单ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    menu_type   CHAR(1)      NOT NULL DEFAULT 'M' COMMENT '菜单类型（M=目录 C=菜单 F=按钮）',
    path        VARCHAR(200)          DEFAULT NULL COMMENT '路由地址',
    component   VARCHAR(255)          DEFAULT NULL COMMENT '组件路径',
    query       VARCHAR(255)          DEFAULT NULL COMMENT '路由参数',
    permission  VARCHAR(100)          DEFAULT NULL COMMENT '权限标识（如sys:user:list）',
    icon        VARCHAR(100)          DEFAULT NULL COMMENT '菜单图标',
    order_num   INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见（0=隐藏 1=可见）',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='菜单/权限表';

-- ----------------------------
-- 5. 用户角色关联表
-- ----------------------------
CREATE TABLE sys_user_role
(
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_role_id (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';

-- ----------------------------
-- 6. 角色菜单关联表
-- ----------------------------
CREATE TABLE sys_role_menu
(
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_menu_id (menu_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色菜单关联表';

-- ----------------------------
-- 7. 角色部门关联表（数据权限-自定义数据范围时使用）
-- ----------------------------
CREATE TABLE sys_role_dept
(
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (role_id, dept_id),
    INDEX idx_dept_id (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色部门关联表';

-- ----------------------------
-- 8. 字典类型表
-- ----------------------------
CREATE TABLE sys_dict_type
(
    id          BIGINT       NOT NULL COMMENT '字典类型ID',
    dict_name   VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type   VARCHAR(100) NOT NULL COMMENT '字典类型（如sys_user_sex）',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_dict_type (dict_type),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典类型表';

-- ----------------------------
-- 9. 字典数据表
-- ----------------------------
CREATE TABLE sys_dict_data
(
    id          BIGINT       NOT NULL COMMENT '字典数据ID',
    dict_type   VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(100) NOT NULL COMMENT '字典值',
    css_class   VARCHAR(100)          DEFAULT NULL COMMENT '样式属性',
    list_class  VARCHAR(100)          DEFAULT NULL COMMENT '表格回显样式',
    is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认（0=否 1=是）',
    order_num   INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=启用 1=禁用）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_dict_type (dict_type),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典数据表';

-- ----------------------------
-- 10. 系统配置表
-- ----------------------------
CREATE TABLE sys_config
(
    id          BIGINT       NOT NULL COMMENT '配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_key  VARCHAR(100) NOT NULL COMMENT '配置键名',
    config_value VARCHAR(500) NOT NULL COMMENT '配置键值',
    config_type TINYINT      NOT NULL DEFAULT 0 COMMENT '配置类型（0=系统 1=自定义）',
    create_by   BIGINT                DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT                DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记（0=正常 1=删除）',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_config_key (config_key),
    INDEX idx_del_flag (del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统配置表';

-- ----------------------------
-- 11. 操作日志表
-- ----------------------------
CREATE TABLE sys_oper_log
(
    id            BIGINT       NOT NULL COMMENT '日志ID',
    title         VARCHAR(100) NOT NULL COMMENT '操作模块',
    business_type TINYINT      NOT NULL DEFAULT 0 COMMENT '业务类型（0=其他 1=新增 2=修改 3=删除 4=授权 5=导出 6=导入 7=强退 8=清空）',
    method        VARCHAR(200) NOT NULL COMMENT '请求方法',
    request_method VARCHAR(10) NOT NULL COMMENT '请求方式（GET/POST等）',
    oper_url      VARCHAR(500) NOT NULL COMMENT '请求URL',
    oper_ip       VARCHAR(128)          DEFAULT NULL COMMENT '操作IP',
    oper_location VARCHAR(100)          DEFAULT NULL COMMENT '操作地点',
    oper_param    TEXT                  DEFAULT NULL COMMENT '请求参数',
    json_result   TEXT                  DEFAULT NULL COMMENT '返回结果',
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '操作状态（0=成功 1=失败）',
    error_msg     TEXT                  DEFAULT NULL COMMENT '错误信息',
    cost_time     BIGINT       NOT NULL DEFAULT 0 COMMENT '消耗时间（毫秒）',
    oper_by       BIGINT                DEFAULT NULL COMMENT '操作人ID',
    oper_name     VARCHAR(50)           DEFAULT NULL COMMENT '操作人',
    oper_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    INDEX idx_oper_time (oper_time),
    INDEX idx_oper_by (oper_by),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

-- ----------------------------
-- 12. 登录日志表
-- ----------------------------
CREATE TABLE sys_login_info
(
    id          BIGINT       NOT NULL COMMENT '登录日志ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    ip_address  VARCHAR(128)          DEFAULT NULL COMMENT '登录IP',
    login_location VARCHAR(100)       DEFAULT NULL COMMENT '登录地点',
    browser     VARCHAR(100)          DEFAULT NULL COMMENT '浏览器',
    os          VARCHAR(100)          DEFAULT NULL COMMENT '操作系统',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '登录状态（0=成功 1=失败）',
    msg         VARCHAR(500)          DEFAULT NULL COMMENT '提示消息',
    login_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='登录日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化部门
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, order_num, leader, phone, status)
VALUES (1, 0, '0', '总公司', 0, '管理员', '13800000000', 0),
       (100, 1, '0,1', '研发部', 1, '研发负责人', '13800000001', 0),
       (101, 100, '0,1,100', '后端组', 1, NULL, NULL, 0),
       (102, 100, '0,1,100', '前端组', 2, NULL, NULL, 0),
       (200, 1, '0,1', '市场部', 2, '市场负责人', NULL, 0),
       (300, 1, '0,1', '运维部', 3, '运维负责人', NULL, 0);

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status)
VALUES (1, '超级管理员', 'admin', 1, 1, 0),
       (2, '普通用户', 'user', 2, 5, 0);

-- 初始化用户（密码均为 123456 的 BCrypt 加密值）
INSERT INTO sys_user (id, dept_id, username, password, nickname, email, phone, gender, status)
VALUES (1, 100, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', 'admin@istream.com', '13800000000', 1, 0),
       (2, 101, 'dev', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '开发者', 'dev@istream.com', '13800000001', 1, 0);

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);

-- 初始化菜单（系统管理）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, order_num, visible, status)
VALUES
-- 一级目录
(1, 0, '系统管理', 'M', '/system', NULL, NULL, 'system', 1, 1, 0),
-- 用户管理
(100, 1, '用户管理', 'C', '/system/user', 'system/user/index', 'system:user:list', 'user', 1, 1, 0),
(101, 100, '用户查询', 'F', NULL, NULL, 'system:user:query', NULL, 1, 1, 0),
(102, 100, '用户新增', 'F', NULL, NULL, 'system:user:add', NULL, 2, 1, 0),
(103, 100, '用户修改', 'F', NULL, NULL, 'system:user:edit', NULL, 3, 1, 0),
(104, 100, '用户删除', 'F', NULL, NULL, 'system:user:delete', NULL, 4, 1, 0),
(105, 100, '用户导出', 'F', NULL, NULL, 'system:user:export', NULL, 5, 1, 0),
(106, 100, '重置密码', 'F', NULL, NULL, 'system:user:reset-pwd', NULL, 6, 1, 0),
-- 角色管理
(200, 1, '角色管理', 'C', '/system/role', 'system/role/index', 'system:role:list', 'role', 2, 1, 0),
(201, 200, '角色查询', 'F', NULL, NULL, 'system:role:query', NULL, 1, 1, 0),
(202, 200, '角色新增', 'F', NULL, NULL, 'system:role:add', NULL, 2, 1, 0),
(203, 200, '角色修改', 'F', NULL, NULL, 'system:role:edit', NULL, 3, 1, 0),
(204, 200, '角色删除', 'F', NULL, NULL, 'system:role:delete', NULL, 4, 1, 0),
-- 菜单管理
(300, 1, '菜单管理', 'C', '/system/menu', 'system/menu/index', 'system:menu:list', 'menu', 3, 1, 0),
(301, 300, '菜单查询', 'F', NULL, NULL, 'system:menu:query', NULL, 1, 1, 0),
(302, 300, '菜单新增', 'F', NULL, NULL, 'system:menu:add', NULL, 2, 1, 0),
(303, 300, '菜单修改', 'F', NULL, NULL, 'system:menu:edit', NULL, 3, 1, 0),
(304, 300, '菜单删除', 'F', NULL, NULL, 'system:menu:delete', NULL, 4, 1, 0),
-- 部门管理
(400, 1, '部门管理', 'C', '/system/dept', 'system/dept/index', 'system:dept:list', 'dept', 4, 1, 0),
(401, 400, '部门查询', 'F', NULL, NULL, 'system:dept:query', NULL, 1, 1, 0),
(402, 400, '部门新增', 'F', NULL, NULL, 'system:dept:add', NULL, 2, 1, 0),
(403, 400, '部门修改', 'F', NULL, NULL, 'system:dept:edit', NULL, 3, 1, 0),
(404, 400, '部门删除', 'F', NULL, NULL, 'system:dept:delete', NULL, 4, 1, 0),
-- 字典管理
(500, 1, '字典管理', 'C', '/system/dict', 'system/dict/index', 'system:dict:list', 'dict', 5, 1, 0),
(501, 500, '字典查询', 'F', NULL, NULL, 'system:dict:query', NULL, 1, 1, 0),
(502, 500, '字典新增', 'F', NULL, NULL, 'system:dict:add', NULL, 2, 1, 0),
(503, 500, '字典修改', 'F', NULL, NULL, 'system:dict:edit', NULL, 3, 1, 0),
(504, 500, '字典删除', 'F', NULL, NULL, 'system:dict:delete', NULL, 4, 1, 0),
-- 系统配置
(600, 1, '系统配置', 'C', '/system/config', 'system/config/index', 'system:config:list', 'config', 6, 1, 0),
(601, 600, '配置查询', 'F', NULL, NULL, 'system:config:query', NULL, 1, 1, 0),
(602, 600, '配置新增', 'F', NULL, NULL, 'system:config:add', NULL, 2, 1, 0),
(603, 600, '配置修改', 'F', NULL, NULL, 'system:config:edit', NULL, 3, 1, 0),
(604, 600, '配置删除', 'F', NULL, NULL, 'system:config:delete', NULL, 4, 1, 0),
-- 操作日志
(700, 1, '操作日志', 'C', '/system/operlog', 'system/operlog/index', 'system:oper-log:list', 'log', 7, 1, 0),
(701, 700, '日志查询', 'F', NULL, NULL, 'system:oper-log:query', NULL, 1, 1, 0),
(702, 700, '日志删除', 'F', NULL, NULL, 'system:oper-log:delete', NULL, 2, 1, 0),
(703, 700, '日志清空', 'F', NULL, NULL, 'system:oper-log:clean', NULL, 3, 1, 0),
-- 登录日志
(800, 1, '登录日志', 'C', '/system/loginInfo', 'system/loginInfo/index', 'system:login-info:list', 'loginInfo', 8, 1, 0),
(801, 800, '日志查询', 'F', NULL, NULL, 'system:login-info:query', NULL, 1, 1, 0),
(802, 800, '日志删除', 'F', NULL, NULL, 'system:login-info:delete', NULL, 2, 1, 0),
(803, 800, '日志清空', 'F', NULL, NULL, 'system:login-info:clean', NULL, 3, 1, 0);

-- 初始化角色菜单关联（超级管理员拥有所有菜单权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id
FROM sys_menu
WHERE del_flag = 0;

-- 初始化字典类型
INSERT INTO sys_dict_type (id, dict_name, dict_type, status)
VALUES (1, '用户性别', 'sys_user_sex', 0),
       (2, '菜单类型', 'sys_menu_type', 0),
       (3, '数据范围', 'sys_data_scope', 0),
       (4, '系统状态', 'sys_status', 0),
       (5, '操作类型', 'sys_oper_type', 0),
       (6, '登录状态', 'sys_login_status', 0);

-- 初始化字典数据
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, is_default, order_num, status)
VALUES
-- 用户性别
(1, 'sys_user_sex', '男', '1', 0, 1, 0),
(2, 'sys_user_sex', '女', '2', 0, 2, 0),
(3, 'sys_user_sex', '未知', '0', 1, 0, 0),
-- 菜单类型
(4, 'sys_menu_type', '目录', 'M', 0, 1, 0),
(5, 'sys_menu_type', '菜单', 'C', 0, 2, 0),
(6, 'sys_menu_type', '按钮', 'F', 0, 3, 0),
-- 数据范围
(7, 'sys_data_scope', '全部数据', '1', 0, 1, 0),
(8, 'sys_data_scope', '自定义数据', '2', 0, 2, 0),
(9, 'sys_data_scope', '本部门数据', '3', 0, 3, 0),
(10, 'sys_data_scope', '本部门及以下', '4', 0, 4, 0),
(11, 'sys_data_scope', '仅本人数据', '5', 1, 5, 0),
-- 系统状态
(12, 'sys_status', '启用', '0', 1, 1, 0),
(13, 'sys_status', '禁用', '1', 0, 2, 0),
-- 操作类型
(14, 'sys_oper_type', '其他', '0', 0, 1, 0),
(15, 'sys_oper_type', '新增', '1', 0, 2, 0),
(16, 'sys_oper_type', '修改', '2', 0, 3, 0),
(17, 'sys_oper_type', '删除', '3', 0, 4, 0),
-- 登录状态
(18, 'sys_login_status', '成功', '0', 0, 1, 0),
(19, 'sys_login_status', '失败', '1', 0, 2, 0);

-- 初始化系统配置
INSERT INTO sys_config (id, config_name, config_key, config_value, config_type)
VALUES (1, '用户初始密码', 'sys.user.initPassword', '123456', 0),
       (2, '登录失败最大次数', 'sys.login.maxRetryCount', '5', 0),
       (3, '登录锁定时间（分钟）', 'sys.login.lockTime', '10', 0),
       (4, '密码最小长度', 'sys.password.minLength', '6', 0),
       (5, '密码最大长度', 'sys.password.maxLength', '20', 0);