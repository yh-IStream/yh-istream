<div align="center">

# yh-istream

**把 AI 织进业务流的中后台全栈底座**

声明式接入 · 渐进式架构 · 传感器 / 执行器 AI · 开箱即用的企业级 RBAC

![JDK](https://img.shields.io/badge/JDK-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-green)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-blue)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen)
![TypeScript](https://img.shields.io/badge/TypeScript-6.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-9.x-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

</div>

---


## 核心特性

| 能力 | 说明 |
|------|------|
| **声明式守护管道** | `@RealTimeSync` 注解,加注解即接入,数据变更自动进入守护管道 |
| **规则引擎** | SpEL 条件表达式评估数据变更事件,检测已知异常,表达式自动缓存 |
| **AI 检测未知异常** | `@AnomalyGuard`(传感器)标注 Service 方法,方法执行后自动调 AI 做语义层检测,补规则盲区 |
| **AI 辅助决策** | Spring AI `@Tool`(执行器)定义可调用的处置操作,AI 建议 → **人工确认** → 执行 |
| **多通道触达** | SSE 实时推送 + 邮件 + 企业微信 + 钉钉 + 飞书 |
| **告警聚合** | 同类告警按时间窗口合并,避免告警风暴;Redis 故障时宁可重推不丢警 |
| **消息中心** | 事件总线 + 持久化 + 10 秒聚合窗口 + 广播已读懒创建 + 未读计数 |
| **SaaS 多租户** | MyBatis-Plus 拦截器自动追加 `tenant_id`,INSERT 自动填充,可按表忽略 |
| **数据权限** | 全部 / 自定义 / 本部门 / 本部门及以下 / 仅本人,五档 SQL 动态注入 + 别名白名单防注入 |
| **渐进式架构** | L0 零依赖 → L1 Redis → L2 Watchdog → L3 AI,每层独立可用 |

## 渐进式架构

起步轻,按需重。每层独立可用,不启用时零开销。

| 级别 | 引入的依赖 | 能力 |
|------|-----------|------|
| L0 | 零外部依赖 | 完整 RBAC + 轮询 |
| L1 | Redis + Redisson | SSE 实时推送 · 分布式限流 · 分布式锁 |
| L2 | Watchdog(可拆卸) | SpEL 规则引擎 · 告警聚合 · 多通道推送 |
| L3 | AI Provider(可拆卸) | `@AnomalyGuard` 异常检测 · `@Tool` 处置执行 · MCP |

```yaml
istream:
  watchdog:
    enabled: true              # 关闭 = 框架退回纯 CRUD 后台
  ai:
    enabled: false             # 关闭 = JVM 里不存在 AI 模块,零开销
```

## AI 守护

### 传感器 → 决策 → 执行,三权分立

```
@AnomalyGuard(传感器)                     @Tool(执行器)
──────────────────────                   ──────────────────────
标注在 Service 方法上                     标注在工具方法上
方法执行成功后 → 异步调 AI 检测            定义 AI / 人可调用的处置操作
发现异常 → 发布事件 → 推送 + 持久化        AI 建议的操作映射到 @Tool 方法
                                         → 人工确认 → 反射执行
```

端到端流程:

```
业务方法返回
   │  @AnomalyGuard 切面拦截
   ▼
异步调用 AI(Spring AI 2.0 ChatClient,OpenRouter / Ollama 零代码切换)
   │  结构化结果:结论 + 置信度 + 建议操作 + 参数
   ▼
置信度达标? ──否──→ 仅记录日志,不推送
   │ 是
   ▼
发布 AnomalyGuardEvent(record 事件,跨模块解耦)
   ├──→ AI 模块:建议入库 sys_ai_suggestion + SSE 推送告警接收人
   └──→ Watchdog:置信度定级 → 聚合去重 → 多通道路由
   ▼
前端实时卡片 → 人工点击「确认执行」
   ▼
McpToolRegistry 按方法名匹配,反射调用对应 @Tool
```

**安全红线:AI 产生的所有处置建议,必须经人工确认后才会执行——这是全局策略,暂时不依赖任何注解属性。**

### 关键注解使用示例

**分工原则:金额超标这类确定性阈值判断,交给 L2 规则引擎(SpEL 表达式,如 `#data['orderAmount'] >= 50000`);`@AnomalyGuard` 只做表达式无法枚举的语义判断。**

```java
// 传感器:检测规则无法穷举的语义风险(contextVars 声明哪些方法参数进入 AI 分析上下文)
@AnomalyGuard(
    prompt = """
            分析订单备注与收货信息,识别变体话术与组合语义风险:
            1. 备注是否存在引流/诈骗话术的变体(谐音、拆字、拼音混写、符号间隔等规避关键词过滤的手段)
            2. 收货人姓名 + 电话 + 留言的组合是否暗示绕过平台的线下交易
            3. 证据不足时输出低置信度,不要硬给结论
            """,
    confidence = 0.7,
    contextVars = {"orderRemark", "receiverName", "receiverPhone", "receiverAddress"}
)
public Order createOrder(Order order) { ... }

// 执行器:AI 可建议的处置操作(人工确认后由 McpToolRegistry 反射执行)
@Tool(description = "冻结异常实体,阻止继续操作")
public String freezeEntity(Long entityId, String entityType, String reason) { ... }
```

- 需要 **AI 自动监控某个操作** → 用 `@AnomalyGuard`
- 需要 **定义 AI 可以建议的处置操作** → 用 `@Tool`

### 模型接入(二选一,只改配置)

```yaml
# 方式 A:本地 Ollama(免费,离线可用)
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen2.5:7b

# 方式 B:OpenRouter 云端(免费额度,注册获取 key)
spring:
  ai:
    openai:
      base-url: https://openrouter.ai/api/v1
      api-key: ${OPENROUTER_API_KEY}
      chat:
        model: dots-studio/dots-3-note-preview:free

istream:
  ai:
    enabled: true
```

未检测到模型时自动降级为 NoOp 提供者,启动不报错;AI 关闭时切面不注册,零开销。

## 内置功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **系统管理** | 用户管理 | 增删改查、分配角色、重置密码、导入导出 |
| | 角色管理 | 增删改查、分配菜单权限、五档数据权限 |
| | 菜单管理 | 目录/菜单/按钮三级管理,循环引用校验 |
| | 部门管理 | 树形管理,换父级联更新祖级路径 |
| | 字典管理 | 字典类型 + 字典数据,前端字典驱动渲染 |
| | 参数配置 | 系统参数键值对管理,带缓存 |
| | 文件管理 | 上传/下载/预览,MIME 白名单 + 魔数校验 |
| **SaaS 多租户** | 租户隔离 | SQL 自动追加 tenant_id,可开关 |
| **守护体系** | SSE 实时推送 | 心跳保活 + 一次性 ticket 鉴权 |
| | 消息中心 | 聚合去重 + 未读计数 + 三层推送保障 |
| | 规则守护(Watchdog) | 规则引擎 + 多通道推送 + 告警聚合 |
| | AI 守护 | 传感器 + 执行器,建议入库 + 实时推送 |
| **系统监控** | 操作日志 | 注解声明式采集,支持 Excel 导出 |
| | 登录日志 | 登录行为记录,IP 离线归属地解析 |
| **开发工具** | 代码生成 | 在线预览/批量生成/下载,连菜单权限一起生成 |

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 核心框架(JDK 21) |
| Spring AI | 2.0.0 | AI 模型调用与 Tool 注册 |
| MyBatis-Plus | 3.5.17 | ORM 框架 |
| Sa-Token | 1.46.0 | 轻量权限认证 |
| Redisson | 4.7.0 | 分布式锁、限流、缓存、SSE 凭证 |
| Flyway | — | 数据库版本迁移 |
| MySQL | 8.0+ / 9.x | 主数据库 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | Composition API + 文件路由 |
| TypeScript | 6.0.x | 类型安全 |
| Naive UI | 2.45.x | Vue 3 组件库,按需注册 |
| Vite | 8.2.x | 构建工具 |
| Pinia | 4.0.x | 状态管理 |
| UnoCSS | 66.x | 原子化 CSS 引擎 |

前端路由基于 `vue-router/vite` 文件路由插件,`src/views` 目录结构即路由结构,无需手动配置路由表。

## 快速开始

### 环境要求

| 依赖 | 版本 | 必选 |
|------|------|------|
| JDK | 25 | ✅ |
| Maven | 3.9+ | ✅ |
| Node.js | 20+ | ✅ |
| MySQL | 8.0+ / 9.x | ✅ |
| Redis | 7.0+ | ✅L1+ 推荐 |
| AI 模型 | OpenRouter Key 或本地 Ollama | 可选 |

### 1. 克隆项目

```bash
git clone https://gitee.com/istream/yh-istream.git
cd yh-istream
```

### 2. 初始化数据库

项目使用 Flyway 自动迁移,**启动后端时自动创建全部表结构与种子数据**(推荐)。

> 注意:开发环境默认连接 `localhost:3307`,按需修改 `yh-istream-web/src/main/resources/application-dev.yml` 中的数据源配置。

也可手动执行各模块下的 SQL 文件:

```bash
yh-istream-{system|message|ai}/src/main/resources/db/migration/*.sql
```

### 3. 启动后端(需先启动 Redis)

```bash
# AI 功能为可选,不需要可跳过。设置模型 Key(Windows PowerShell):
$env:OPENROUTER_API_KEY="sk-or-v1-xxx"
# Linux / macOS:
export OPENROUTER_API_KEY="sk-or-v1-xxx"

mvn clean compile
mvn spring-boot:run -pl yh-istream-server
```

后端启动后:

- API 地址:`http://localhost:8080/api/v1`
- API 文档:`http://localhost:8080/api/v1/doc.html`

### 4. 启动前端

```bash
cd yh-istream-ui/yh-istream-web
npm install
npm run dev
```

访问:`http://localhost:5173`

### 5. 默认账号

| 账号 | 密码 | 说明 |
|------|------|------|
| admin | 123456 | 超级管理员 |

### 6. 试用 AI 守护

1. 登录后进入左侧菜单「AI 守护中心」
2. 在「告警接收人」中添加你的用户 ID
3. 切换到「真实 AI 检测」,选择预设场景触发
4. 等待 AI 响应(约 5-10 秒),看板展示分析结果,右上角铃铛弹出通知
5. 点击卡片「确认执行」完成处置闭环

> 未配置模型 Key?切换到「模拟触发」页签,可体验完整交互闭环。

### Docker 部署

```bash
cp .env.example .env    # 填入 MySQL / Redis 密码
docker compose up -d
```

## 界面预览

| 登录页 | 用户管理 |
|--------|----------|
| ![登录页](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%99%BB%E5%BD%95.png) | ![用户管理](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E7%94%A8%E6%88%B7.png) |

| 字典管理 | AI 守护中心 |
|----------|-------------|
| ![字典](https://raw.gitcode.com/IStream/image/raw/image/yh-is/%E5%AD%97%E5%85%B8.png) | ![AI](https://raw.gitcode.com/IStream/image/raw/image/yh-is/3232332.png) |

## Roadmap

- [ ] AI 提示词外置:模板入库 + 注解 key 绑定,支持对接 Langfuse 做版本管理与调用观测
- [ ] 知识库集成(不自研平台):对接 Dify / MaxKB 的可选模块;内嵌轻量语义检索基于 Spring AI VectorStore
- [ ] `@AnomalyGuard` 范式延伸官方示例:文本内容审核、工单语义分类、日志异常聚类
- [ ] 告警升级策略与告警持久化
- [ ] 对象存储扩展:MinIO / 阿里云 OSS / 腾讯云 COS
- [ ] 移动端(uni-app)

## 支持

如果这个项目对你有帮助,欢迎点个 Star ⭐,你的支持是持续更新的动力!

## 🤝 参与贡献

欢迎提交 Pull Request 或 Issue！

> - 1. Fork 本仓库
> - 2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
> - 3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
> - 4. 推送到分支 (`git push origin feature/AmazingFeature`)
> - 5. 提交 Pull Request

---

## License

[MIT](./LICENSE)
