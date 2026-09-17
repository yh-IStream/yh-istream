<h1 align="center">iStream 企业级快速开发框架</h1>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-orange" alt="JDK 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/Vue-3.5-brightgreen" alt="Vue 3" />
  <img src="https://img.shields.io/badge/TypeScript-6.0-blue" alt="TypeScript 6.0" />
  <img src="https://img.shields.io/badge/MySQL-9.x-blue" alt="MySQL 9" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
</p>

---

## 项目简介

这是一个以**代码质量为第一优先级**的企业级快速开发框架。

采用前后端分离架构，后端基于 Spring Boot 4 + MyBatis-Plus + Sa-Token + Redisson，前端基于 Vue 3 + TypeScript+ Naive UI + Vite。内置 RBAC 权限管理、SaaS 多租户、数据权限、代码生成器等核心功能，开箱即用。

### 在线预览

![登录页](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%99%BB%E5%BD%95.png)

![用户管理](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%94%A8%E6%88%B7.png)

![字典](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E5%AD%97%E5%85%B8.png)

---

## 设计理念

### 🏗️ 架构精简

- **7 个模块，职责清晰** — 不过度拆分，每个模块有明确的边界
- **DTO/Entity 严格分离** — 编译期生成转换代码，Entity 禁止暴露给前端，杜绝字段泄露
- **CacheService 统一缓存抽象** — 封装 Redisson，修改缓存策略只改一处
- **Controller 零业务逻辑** — 分层严格，调试时跳转层级最少，改一处不用动全局

### ⚡ 性能优化

- **只读事务优化**
- **缓存批量操作**
- **编译期对象映射** — 零反射开销，对比运行时反射性能提升 10x+

### 🌐 多租户零感知

- **SQL 自动隔离** — MyBatis-Plus TenantLineInnerInterceptor 自动追加 `tenant_id` 条件，开发者无需手动处理
- **上下文自动传递** — TenantFilter → TenantContext(ThreadLocal) → TenantLineHandlerImpl，请求级自动流转
- **INSERT 自动填充** — MetaObjectHandler 自动填充 tenantId，无需业务代码感知
- **零遗漏风险** — 框架级拦截，不存在忘记加过滤条件导致越权的可能


## 核心特性

### 权限与安全

| 特性 | 说明 |
|------|------|
| **RBAC 权限模型** | 用户-角色-菜单-部门四级权限，菜单权限 + 数据权限双重控制 |
| **SaaS 多租户** | MyBatis-Plus TenantLineInnerInterceptor 自动追加 `tenant_id` 条件，开发者零感知、零遗漏 |
| **数据权限** | 基于注解 `@DataScope` + AOP 自动注入 SQL 条件，按部门层级隔离数据 |
| **超级管理员保护** | 删除/禁用操作均校验 `checkSuperAdminRole()`，防止误操作 |
| **接口限流** | 基于注解 `@RateLimit` + Redisson 分布式限流 |

### 开发效率

| 特性 | 说明 |
|------|------|
| **代码生成器** | 在线预览 / 批量生成 / 下载代码，一键生成 Entity-Mapper-Service-Controller 全套代码 |
| **统一缓存抽象** | CacheService 接口封装 Redisson，支持 Pipeline 批量操作、模式匹配删除、原子计数器 |

### 运维监控

| 特性 | 说明 |
|------|------|
| **操作日志** | 基于注解 `@OperLog` + Spring Event 异步记录，支持 Excel 导出 |
| **登录日志** | 自动记录登录行为，IP 归属地解析 |
| **SSE 实时推送** | 基于 Server-Sent Events 的实时消息推送 |
| **文件存储** | 本地存储 / MinIO / 阿里云 OSS 可切换，统一 FileStorageService 接口 |
| **Excel 导入导出** | 基于 EasyExcel，支持大数据量流式导出 |
| **分布式定时任务** | XXL-Job 集成，支持可视化任务调度 |

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 核心框架 |
| MyBatis-Plus | 3.5.17 | ORM 框架 |
| Sa-Token | 1.46.0 | 轻量权限认证框架 |
| Redisson | 4.7.0 | 分布式锁、限流、缓存 |
| Hutool | 5.8.47 | 工具集（BCrypt、IO、日期等） |
| MapStruct | 1.6.3 | 对象映射 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| XXL-Job | 3.4.2 | 分布式定时任务 |
| Knife4j | 5.2.3 | API 文档（Swagger 增强） |
| Flyway | — | 数据库版本迁移（仅 V1 初始化） |
| MySQL | 9.7.2 | 主数据库（兼容 PostgreSQL） |
| Redis | 7.0+ | 缓存 / 会话存储 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式框架，Composition API |
| TypeScript | 6.0.x | 类型安全 |
| Naive UI | 2.45.x | Vue 3 组件库 |
| Vite | 8.2.x | 构建工具 |
| Pinia | 4.0.x | 状态管理 |
| Vue Router | 5.2.x | 路由（基于文件路由） |
| UnoCSS | 66.7.x | 原子化 CSS 引擎 |

### 文件路由

基于 `vue-router/vite` 插件，`src/views` 目录结构即路由结构，无需手动配置路由表。

---

## 项目结构

```
yh-istream
├── yh-istream-common        # 通用模块：异常、枚举、注解、基础模型
├── yh-istream-framework     # 框架模块：安全、缓存、租户、AOP、全局异常处理
├── yh-istream-system        # 系统模块：用户/角色/菜单/部门/字典/日志等核心业务
├── yh-istream-web           # Web 模块：Controller、认证、Dashboard
├── yh-istream-generator     # 代码生成器模块
├── yh-istream-job           # 定时任务模块（XXL-Job）
├── yh-istream-file          # 文件存储模块（本地/MinIO/OSS）
└── yh-istream-ui            # 前端
    └── yh-istream-web       # Vue 3 + TypeScript + Naive UI
```

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

| 账号 | 密码     | 说明 |
|------|--------|------|
| admin | 123456 | 超级管理员 |

---

## Docker 部署

### 一键启动（推荐）

```bash
# 使用 docker-compose 启动全部服务（MySQL 9.2 + Redis 7.4 + App）
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
# 先打包
mvn clean package -DskipTests

# 构建镜像（基于 Eclipse Temurin JRE 21 + ZGC）
docker build -t yh-istream:latest .
```

---

## 内置功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **系统管理** | 用户管理 | 用户增删改查、分配角色、重置密码、导入导出 |
| | 角色管理 | 角色增删改查、分配菜单权限、分配数据权限、分配用户 |
| | 菜单管理 | 菜单/目录/按钮三级管理，树形展示，循环引用校验 |
| | 部门管理 | 部门树形管理，循环引用校验，支持数据权限隔离 |
| | 字典管理 | 字典类型 + 字典数据管理，前端字典驱动渲染 |
| | 参数配置 | 系统参数键值对管理 |
| | 文件管理 | 文件上传/下载/预览，支持本地/MinIO/OSS |
| **SaaS 多租户** | 租户隔离 | SQL 自动追加 tenant_id，上下文 ThreadLocal 传递，INSERT 自动填充 |
| **系统监控** | 操作日志 | 操作日志查询，支持 Excel 导出 |
| | 登录日志 | 登录行为记录，IP 归属地解析 |
| **开发工具** | 代码生成 | 在线预览 / 批量生成 / 下载代码 |

---

## 支持点个 Star ⭐

如果这个项目对你有帮助，欢迎点个 Star 支持一下，你的支持是持续更新的动力！

---

## License

[MIT](./LICENSE)