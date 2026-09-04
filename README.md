<h1 align="center">iStream 企业级智能流式快速开发框架</h1>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-orange" alt="JDK 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/Vue-3.5-brightgreen" alt="Vue 3" />
  <img src="https://img.shields.io/badge/MySQL-9.x-blue" alt="MySQL 9" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
</p>

---

## 项目简介

采用前后端分离架构，内置 RBAC 权限管理、数据权限、代码生成器、操作日志、登录日志、文件存储、定时任务等核心功能。

### 在线预览

![登录页](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%99%BB%E5%BD%95.png)

![用户管理](https://raw.gitcode.com/IStream/image/raw/image/yh-is//%E7%94%A8%E6%88%B7.png)

![字典](https://raw.gitcode.com/IStream/image/raw/image/yh-is//%E5%AD%97%E5%85%B8.png)

---

## 核心特性

| 特性 | 说明 |
|------|------|
| **RBAC 权限模型** | 用户-角色-菜单-部门四级权限，支持菜单权限 + 数据权限双重控制 |
| **数据权限** | 基于注解 `@DataScope` 自动注入 SQL 条件，按部门层级隔离数据 |
| **代码生成器** | 在线预览 / 批量生成 / 下载代码，一键生成 Entity-Mapper-Service-Controller 全套代码 |
| **操作日志** | 基于注解 `@OperLog` + Spring Event 异步记录，支持 Excel 导出 |
| **登录日志** | 自动记录登录行为，IP 归属地解析 |
| **接口限流** | 基于注解 `@RateLimit` + Redisson 分布式限流 |
| **SSE 实时推送** | 基于 Server-Sent Events 的实时消息推送 |
| **文件存储** | 本地存储 / MinIO / 阿里云 OSS 可切换，统一接口 |
| **Excel 导入导出** | 基于 EasyExcel，支持大数据量流式导出 |
| **密码加密** | BCrypt 加密|
| **Docker 部署** | 提供 Dockerfile + docker-compose，一键容器化部署 |
| **CI/CD** | GitHub Actions 自动构建测试 + JaCoCo 覆盖率上报 |

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

项目使用 Flyway 自动迁移，启动后端时自动创建表结构(自行修改数据库端口)。也可手动执行：

```bash
# SQL 文件位于
yh-istream-admin/src/main/resources/db/migration/V1_init_schema1.sql
```

### 3. 启动后端（需要先启动redis）

```bash
# 修改数据库连接信息
vim yh-istream-admin/src/main/resources/application-dev.yml

# 编译启动
mvn clean compile
mvn spring-boot:run -pl yh-istream-admin
```

后端启动成功后访问：`http://localhost:8080/api/v1`

API 文档访问：`http://localhost:8080/api/v1/doc.html`

### 4. 启动前端

```bash
cd yh-istream-ui/yh-istream-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动成功后访问：`http://localhost:5173`

### 5. 默认账号

| 账号 | 密码 | 说明 |
|------|------|------|
| admin | admin123 | 超级管理员 |

---

## Docker 部署

### 一键启动（推荐）

```bash
# 使用 docker-compose 启动全部服务（MySQL + Redis + App）
docker-compose up -d
```

### 自定义环境变量

```bash
# 创建 .env 文件
MYSQL_ROOT_PASSWORD=your_password
REDIS_PASSWORD=your_redis_password
```

### 单独构建后端镜像

```bash
# 先打包
mvn clean package -DskipTests

# 构建镜像
docker build -t yh-istream:latest .
```

---

## License

[MIT](./LICENSE)