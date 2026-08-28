<p align="center">
  <h1 align="center">yh-istream</h1>
</p>

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 运行环境 |
| Spring Boot | 4.1.0 | 核心框架 |
| MyBatis-Plus | 3.5.17 | ORM 框架 |
| Sa-Token | 1.45.0 | 认证与授权 |
| Redis + Redisson | 7.4 / 4.7.0 | 缓存与分布式锁 |
| MySQL | 9.x | 主数据库（也支持 PostgreSQL） |
| Flyway | — | 数据库版本迁移 |
| Knife4j | 5.2.3 | API 文档 |
| XXL-Job | 3.4.2 | 分布式任务调度 |
| Hutool | 5.8.47 | Java 工具库 |
| MapStruct | 1.6.3 | 对象映射 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| Maven | 3.x | 构建工具 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.41 | 前端框架 |
| Vite | 8.2.2 | 构建工具 |
| Naive UI | 2.45.0 | UI 组件库 |
| UnoCSS | 66.x | 原子化 CSS |
| Pinia | 4.0.3 | 状态管理 |
| Vue Router | 5.2.0 | 路由（文件系统路由） |
| TypeScript | 6.0.x | 类型安全 |
| Axios | 1.19.0 | HTTP 客户端 |

---

## 项目结构

```
yh-istream/
├── yh-istream-common/          # 通用模块：常量、枚举、异常、DTO、工具类
├── yh-istream-framework/       # 框架核心：AOP、全局异常处理、SSE、安全工具
├── yh-istream-system/          # 业务核心：用户、角色、菜单、部门、字典、日志
├── yh-istream-admin/           # 启动入口：启动类、配置文件、Flyway 迁移脚本
├── yh-istream-generator/       # 代码生成器：基于 Velocity 模板引擎
├── yh-istream-job/             # 定时任务：基于 XXL-Job 分布式调度
├── yh-istream-oss/             # 对象存储：本地存储（可扩展云存储）
├── yh-istream-ui/
│   └── yh-istream-admin/       # 前端管理后台：Vue 3 + Naive UI
├── docker-compose.yml          # Docker 容器编排
├── Dockerfile                  # 应用镜像构建
└── pom.xml                     # Maven 父 POM
```

### 模块依赖关系

```
yh-istream-common
       ↑
yh-istream-framework
       ↑
yh-istream-system  ←  yh-istream-oss  ←  yh-istream-job
       ↑                  ↑                 ↑
       └──────────────────┴─────────────────┘
                         ↑
                  yh-istream-admin（启动入口）
                         ↑
                  yh-istream-generator
```

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+ 或 PostgreSQL 14+
- Redis 6.0+
- Node.js 20+（前端开发）

### 1. 启动中间件

使用 Docker Compose 一键启动 MySQL 和 Redis：

```bash
docker-compose up -d mysql redis
```

### 2. 初始化数据库

项目使用 Flyway 自动管理数据库迁移。首次启动应用时会自动执行建表脚本和初始化数据。

默认管理员账号：
- 用户名：`admin`
- 密码：`123456`

### 3. 启动后端

```bash
# 开发环境（默认 profile: dev）
mvn clean install -DskipTests
cd yh-istream-admin
mvn spring-boot:run

# 或指定环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

后端默认运行在 `http://localhost:8080/api/v1`

### 4. 启动前端

```bash
cd yh-istream-ui/yh-istream-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 5. 访问 API 文档

启动后访问 Knife4j 接口文档：

- 开发环境：`http://localhost:8080/api/v1/doc.html`

---

## 系统功能

### 权限管理

- **用户管理**：用户增删改查、状态管理、密码重置
- **角色管理**：角色分配、菜单权限、数据权限
- **菜单管理**：目录/菜单/按钮三级权限控制
- **部门管理**：树形组织结构

### 数据权限

支持五种数据权限范围：
- 全部数据权限
- 自定义数据权限
- 本部门数据权限
- 本部门及以下数据权限
- 仅本人数据权限

### 系统功能

- **字典管理**：系统字典类型与数据维护
- **参数配置**：系统参数键值对管理
- **文件管理**：支持本地存储（可扩展云存储）
- **操作日志**：基于 AOP 自动记录，支持 SSE 实时推送
- **登录日志**：登录状态、IP、归属地记录

### 代码生成器

- 选择数据库表，自动生成全套 CRUD 代码
- 支持 Controller、Service、Mapper、Entity、Migration SQL
- 支持预览和批量下载

### 定时任务

- 基于 XXL-Job 分布式任务调度
- 内置日志清理任务

---

## 多环境配置

| 配置文件 | 环境 | 说明 |
|----------|------|------|
| `application.yml` | 通用 | 公共配置 |
| `application-dev.yml` | 开发 | 本地开发，开启 DEBUG 日志和 SQL 日志 |
| `application-prod.yml` | 生产 | 生产环境，关闭文档，启用 Prometheus 监控 |

切换环境：

```bash
# 方式一：启动参数
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 方式二：环境变量
export SPRING_PROFILES_ACTIVE=prod

# 方式三：修改 application.yml
spring.profiles.active: prod
```

---

## Docker 部署

```bash
# 完整部署（MySQL + Redis + 应用）
docker-compose up -d

# 仅启动应用（需要已有 MySQL 和 Redis）
docker-compose up -d app
```

环境变量配置通过 `.env` 文件或 `docker-compose.yml` 中的 `environment` 配置。

---

## 项目规范

- 统一响应体：`R<T>` 类封装 `code`、`msg`、`data`
- 业务异常：`BusinessException` 统一处理
- 分页查询：`BaseQuery` 基类，子类继承扩展
- 实体基类：`BaseEntity` 包含通用字段（id、创建时间、更新时间等）
- 逻辑删除：MyBatis-Plus `delFlag` 字段
- 对象转换：MapStruct 接口定义在 `convert` 包下

---

## 常用命令

```bash
# 后端
mvn clean install -DskipTests          # 编译打包（跳过测试）
mvn test                                # 运行测试
mvn jacoco:report                       # 生成覆盖率报告

# 前端
npm run dev                             # 启动开发服务器
npm run build                           # 生产构建
npm run lint                            # 代码检查
npm run preview                         # 预览构建结果
```

---

## License

MIT
