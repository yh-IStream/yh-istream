<h1 align="center">内置智能守护的中后台全栈底座</h1>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-orange" alt="JDK 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/Vue-3.5-brightgreen" alt="Vue 3" />
  <img src="https://img.shields.io/badge/TypeScript-6.0-blue" alt="TypeScript 6.0" />
  <img src="https://img.shields.io/badge/MySQL-9.x-blue" alt="MySQL 9" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
</p>

---

## 核心能力

| 能力 | 说明 |
|------|------|
| **智能守护** | 规则引擎 + AI 分析 + 告警聚合，业务异常自动检测 |
| **多通道触达** | SSE + 邮件 + 企业微信 + 钉钉 + 飞书 + SMS + Webhook |
| **升级策略** | SSE → 邮件 → 企业微信 → SMS → 电话，确保送达 |
| **声明式接入** | `@RealTimeSync` 注解，加注解即接入守护管道 |
| **告警即行动** | 告警后可直接触发业务操作（催办、转审、冻结、回滚） |
| **AI 增强** | `@AIAnalyze` 发现未知异常，可拆卸 |
| **MCP 就绪** | `@McpTool` 注解，AI Agent 可操作后台，可拆卸 |
| **渐进式架构** | L0 零依赖 → L1 Redis → L2 Watchdog → L3 AI，每层独立可用 |

---

## 智能守护（Watchdog）

业务事件 → 规则/检测 → 多通道触达 → 升级保障。

```
数据变更 / 业务事件
       ↓
  规则引擎 + AI 分析（@AIAnalyze）
       ↓ 发现问题，判断重要性
  告警路由（重要性分级 → 匹配接收人 → 选择通道）
       ↓
  多通道推送（邮件 / 企业微信 / 钉钉 / 飞书 / SMS / SSE / Webhook）
       ↓
  升级策略（5min 未确认 → 升级到更强通道）
```

## 渐进式架构

起步轻，按需重。每层独立可用，不启用时零开销。

| 级别 | 引入的依赖 | 能力 |
|------|-----------|------|
| L0 | 零外部依赖 | 纯轮询，完整 RBAC |
| L1 | Redis + Redisson | SSE 实时推送 + 限流 + 分布式锁 |
| L2 | Watchdog（可拆卸） | 规则引擎 + 多通道推送 + 升级策略 + 告警聚合 |
| L3 | AI Provider（可拆卸） | `@AIAnalyze` 智能洞察，发现未知异常 |

```yaml
istream:
  watchdog:
    enabled: true              # 关闭 = 框架退回纯 CRUD 后台
  ai:
    enabled: false             # 关闭 = JVM 里不存在 AI 模块，零开销
```

## 在线预览

![登录页](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%99%BB%E5%BD%95.png)

![用户管理](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%94%A8%E6%88%B7.png)

![字典](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E5%AD%97%E5%85%B8.png)

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 核心框架 |
| MyBatis-Plus | 3.5.17 | ORM 框架 |
| Sa-Token | 1.46.0 | 轻量权限认证框架 |
| Redisson | 4.7.0 | 分布式锁、限流、缓存、Redis Stream 广播 |
| Spring AI | — | AI 集成（可拆卸，optional） |
| Hutool | 5.8.47 | 工具集（BCrypt、IO、日期等） |
| MapStruct | 1.6.3 | 对象映射 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| Knife4j | 5.2.3 | API 文档（Swagger 增强） |
| Flyway | — | 数据库版本迁移（仅 V1 初始化） |
| MySQL | 9.7.2 | 主数据库 |
| Redis | 7.0+ | 缓存 / 会话 / 消息总线 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式框架，Composition API |
| TypeScript | 6.0.x | 类型安全 |
| Naive UI | 2.45.x | Vue 3 组件库，TreeShakable |
| Vite | 8.2.x | 构建工具 |
| Pinia | 4.0.x | 状态管理 |
| Vue Router | 5.2.x | 基于文件路由 |
| UnoCSS | 66.7.x | 原子化 CSS 引擎 |

### 文件路由

基于 `vue-router/vite` 插件，`src/views` 目录结构即路由结构，无需手动配置路由表。


---

## 快速开始

### 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| MySQL | 8.0+ / 9.x |
| Redis | 7.0+ |

### 1. 克隆项目

```bash
git clone https://gitee.com/istream/yh-istream.git
cd yh-istream
```

### 2. 初始化数据库

项目使用 Flyway 自动迁移，启动后端时自动创建表结构（自行修改数据库端口）。也可手动执行：

```bash
# SQL 文件位于
yh-istream-system/src/main/resources/db/migration/V1_init_schema1.sql
```

### 3. 启动后端（需要先启动 Redis）

```bash
# 修改数据库连接信息（默认端口 3307，按需调整）
vim yh-istream-web/src/main/resources/application-dev.yml

# 编译启动
mvn clean compile
mvn spring-boot:run -pl yh-istream-web
```

后端启动成功后访问：`http://localhost:8080/api/v1`

API 文档访问：`http://localhost:8080/api/v1/doc.html`

### 4. 启动前端

```bash
cd yh-istream-ui/yh-istream-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动成功后访问：`http://localhost:5173`

### 5. 默认账号

| 账号 | 密码 | 说明 |
|------|------|------|
| admin | 123456 | 超级管理员 |

---

## Docker 部署

### 一键启动

```bash
docker-compose up -d
```

### 自定义环境变量

```bash
# 复制模板并修改
cp .env.example .env
vim .env
```

### 单独构建后端镜像

```bash
mvn clean package -DskipTests
docker build -t yh-istream:latest .
```

---

## 内置功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **系统管理** | 用户管理 | 用户增删改查、分配角色、重置密码、导入导出 |
| | 角色管理 | 角色增删改查、分配菜单权限、分配数据权限 |
| | 菜单管理 | 菜单/目录/按钮三级管理，树形展示，循环引用校验 |
| | 部门管理 | 部门树形管理，循环引用校验，支持数据权限隔离 |
| | 字典管理 | 字典类型 + 字典数据管理，前端字典驱动渲染 |
| | 参数配置 | 系统参数键值对管理 |
| | 文件管理 | 文件上传/下载/预览，支持本地/MinIO/OSS |
| **SaaS 多租户** | 租户隔离 | SQL 自动追加 tenant_id，INSERT 自动填充 |
| **智能守护** | SSE 实时推送 | 服务端单向推送，心跳保活，ticket 安全机制 |
| | 消息中心 | 事件总线 + 消息持久化 + 消息聚合 + 未读计数 |
| | Watchdog | 规则引擎 + 多通道推送 + 升级策略 + 告警聚合（规划中） |
| **系统监控** | 操作日志 | 操作日志查询，支持 Excel 导出 |
| | 登录日志 | 登录行为记录，IP 归属地解析 |
| **开发工具** | 代码生成 | 在线预览 / 批量生成 / 下载代码 |

---

## 支持点个 Star ⭐

如果这个项目对你有帮助，欢迎点个 Star，你的支持是持续更新的动力！

---

## License

[MIT](./LICENSE)