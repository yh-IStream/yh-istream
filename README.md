<h1 align="center">yh-istream — 内置 AI 智能守护的中后台全栈底座</h1>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-orange" alt="JDK 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/Spring%20AI-2.0.0-blue" alt="Spring AI 2.0" />
  <img src="https://img.shields.io/badge/Vue-3.5-brightgreen" alt="Vue 3" />
  <img src="https://img.shields.io/badge/TypeScript-6.0-blue" alt="TypeScript 6.0" />
  <img src="https://img.shields.io/badge/MySQL-9.x-blue" alt="MySQL 9" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
</p>

---

## 核心能力

| 能力 | 说明 |
|------|------|
| **声明式守护管道** | `@RealTimeSync` 注解，加注解即接入，数据变更自动进入守护管道 |
| **规则引擎** | SpEL 条件表达式，YAML 声明告警规则，检测已知异常 |
| **AI 发现未知异常** | `@AnomalyGuard` 注解（传感器），标注 Service 方法后自动调 AI 检测异常，补规则盲区 |
| **AI 辅助决策** | Spring AI `@Tool` 注解（执行器），定义可调用的处置工具，AI 建议 → 人工确认 → 执行 |
| **多通道触达** | SSE 实时推送 + 邮件 + 企业微信 + 钉钉 + 飞书 + SMS + Webhook |
| **升级策略** | SSE → 邮件 → 企业微信 → SMS，确保送达 |
| **告警聚合** | 同类告警 10 分钟内合并为 1 条，避免告警风暴 |
| **渐进式架构** | L0 零依赖 → L1 Redis → L2 Watchdog → L3 AI，每层独立可用 |

---

## 渐进式架构

起步轻，按需重。每层独立可用，不启用时零开销。

| 级别 | 引入的依赖 | 能力 |
|------|-----------|------|
| L0 | 零外部依赖 | 纯轮询，完整 RBAC |
| L1 | Redis + Redisson | SSE 实时推送 + 限流 + 分布式锁 |
| L2 | Watchdog（可拆卸） | 规则引擎 + 多通道推送 + 升级策略 + 告警聚合 |
| L3 | AI Provider（可拆卸） | `@AnomalyGuard` 异常检测（传感器） + `@Tool` 处置执行（执行器） |

```yaml
istream:
  watchdog:
    enabled: true              # 关闭 = 框架退回纯 CRUD 后台
  ai:
    enabled: false             # 关闭 = JVM 里不存在 AI 模块，零开销
```

---

## AI 智能守护

### 三注解闭环

```
@AnomalyGuard（传感器）                  @Tool（执行器）
─────────────────────                   ─────────────────
标注在 Service 方法上                    标注在工具方法上
方法执行后 → 自动调 AI 检测异常         定义 AI/人 可调用的处置操作
发现异常 → 发布事件 → 推送 + 持久化     AI 建议的操作名 映射到 @Tool 方法
                                      → 人工确认 → 反射调用
```


- 需要 **AI 自动监控某个操作** → 用 `@AnomalyGuard`（如：创建订单后 AI 检测是否异常）
- 需要 **定义 AI 可以建议的处置操作** → 用 `@Tool`（如：冻结订单、退款、发送警告）

### 关键注解使用示例

```java
// 传感器：检测下单是否异常
@AnomalyGuard(
    entityId = "#order.id",
    entityType = "订单",
    contextVars = {"order.amount", "order.userCreditScore", "order.deliveryCity"}
)
public Order createOrder(Order order) { ... }

// 执行器：AI 可建议的处置操作
@Tool(description = "冻结指定订单，阻止发货")
public boolean freezeOrder(@ToolParam(description = "订单ID") Long orderId) { ... }
```

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 核心框架 |
| Spring AI | 2.0.0 | AI 模型调用与 Tool 注册|
| MyBatis-Plus | 3.5.17 | ORM 框架 |
| Sa-Token | 1.46.0 | 轻量权限认证框架 |
| Redisson | 4.7.0 | 分布式锁、限流、缓存、Redis Stream 广播 |
| Hutool | 5.8.47 | 工具集（BCrypt、IO、日期等） |
| MapStruct | 1.6.3 | 对象映射 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| Knife4j | 5.2.3 | API 文档（Swagger 增强） |
| Flyway | — | 数据库版本迁移 |
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

| 依赖 | 版本 | 必选 |
|------|------|------|
| JDK | 21+ | ✅ |
| Maven | 3.9+ | ✅ |
| Node.js | 20+ | ✅ |
| MySQL | 8.0+ / 9.x | ✅ |
| Redis | 7.0+ | L1+ |
| OpenRouter API Key | — | AI 功能 |

### 1. 克隆项目

```bash
git clone https://gitee.com/istream/yh-istream.git
cd yh-istream
```

### 2. 初始化数据库

项目使用 Flyway 自动迁移，启动后端时自动创建表结构（只修改数据库端口即可（目前是3307））（推荐）。

也可手动导入模块下的sql文件：

```bash
# SQL 文件位于模块下的/db/migration
yh-istream-(system/ai/message)/src/main/resources/db/migration/*.sql
```

### 3. 启动后端（需要先启动 Redis）

```bash
# 设置 AI 环境变量（可选，不需要 AI 可跳过）
# Windows PowerShell:
$env:OPENROUTER_API_KEY="sk-or-v1-xxx"
# Linux / macOS:
export OPENROUTER_API_KEY="sk-or-v1-xxx"

# 编译启动
mvn clean compile
mvn spring-boot:run -pl yh-istream-web
```

后端启动后：
- API 地址：`http://localhost:8080/api/v1`
- API 文档：`http://localhost:8080/api/v1/doc.html`

### 4. 启动前端

```bash
cd yh-istream-ui/yh-istream-web
npm install
npm run dev
```

访问：`http://localhost:5173`

### 5. 默认账号

| 账号 | 密码 | 说明 |
|------|------|------|
| admin | 123456 | 超级管理员 |

### 6. 试用 AI 功能

1. 登录后左侧菜单点击「AI 守护中心」
2. 在「告警接收人」tab 中添加你的用户 ID
3. 切换到「真实 AI 检测」tab，修改参数后点击（当前仅写有四个示例。但后端已写好注释，请自行创建你自己的表并在需要检测的service上添加注解，照抄已有示例即可）
4. 等待 ai响应并解析（5-10） 秒，看板卡片显示 AI 分析结果，右上角铃铛弹出通知
5. 点击卡片上的「确认冻结」执行处置

---

## 在线预览

![登录页](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%99%BB%E5%BD%95.png)

![用户管理](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%94%A8%E6%88%B7.png)

![字典](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E5%AD%97%E5%85%B8.png)

![ai](https://raw.gitcode.com/IStream/image/raw/image/yh-is/3232332.png)

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
| | Watchdog | 规则引擎 + 多通道推送 + 升级策略 + 告警聚合 |
| | AI 异常检测 | `@AnomalyGuard` 传感器 + `@Tool` 执行器，AI 建议入库 + SSE 推送 |
| | 告警接收人 | 前端配置接收人列表，AI 检测到异常后推送给所有启用用户 |
| **系统监控** | 操作日志 | 操作日志查询，支持 Excel 导出 |
| | 登录日志 | 登录行为记录，IP 归属地解析 |
| **开发工具** | 代码生成 | 在线预览 / 批量生成 / 下载代码 |

---

## 渐进式部署

```yaml
istream:
  watchdog:
    enabled: true              # L0 关 = 纯 CRUD + SaaS 后台
  ai:
    enabled: false             # 关 = JVM 里不存在 AI 模块，零开销
```

| 级别 | 引入的依赖 | 能力 |
|------|-----------|------|
| L0 | 零外部依赖 | 纯轮询，完整 RBAC |
| L1 | Redis + Redisson | SSE 实时推送 + 限流 + 分布式锁 |
| L2 | Watchdog（可拆卸） | 规则引擎 + 多通道推送 + 升级策略 + 告警聚合 |
| L3 | AI Provider（可拆卸） | `@AnomalyGuard` 异常检测 + `@Tool` 处置执行 |

---

## 支持点个 Star ⭐

如果这个项目对你有帮助，欢迎点个 Star，你的支持是持续更新的动力！

---

## License

[MIT](./LICENSE)