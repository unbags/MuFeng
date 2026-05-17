# 沐枫餐饮 - 后端服务

Spring Boot 后端服务，提供 REST API、JWT 认证、WebSocket 实时推送、Redis 缓存和 AI 点餐助手。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.5.14 | 核心框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis Stack | 7.4 | 菜单缓存 |
| Milvus | 2.5 | AI 知识库向量检索 |
| etcd / MinIO | - | Milvus 元数据与对象存储依赖 |
| Spring Security | - | 认证与授权 |
| JWT | 0.12.6 | HMAC-SHA512 无状态令牌 |
| WebSocket + STOMP | - | 实时推送（订单状态、看板刷新） |
| Spring AI | 1.1.6 | OpenAI 兼容对话、Milvus VectorStore、RAG QuestionAnswerAdvisor |
| Spring AI Alibaba | 1.1.2.2 | DashScope text-embedding-v4 |
| Apache Tika | 2.9.2 | 文档文本提取（PDF/DOCX/MD/TXT/HTML/CSV） |
| Micrometer + Prometheus | - | 应用监控与指标采集 |
| Maven | 3.9 | 项目构建管理 |

## 项目结构

```
backend/
├── src/main/java/com/unbags/ordering/
│   ├── OrderApplication.java    # Spring Boot 入口
│   ├── config/                  # Security、Web MVC、Redis、WebSocket、MyBatis-Plus、限流、异常处理
│   ├── controller/              # REST 控制器（Menu、Cart、Order、Auth、Review、Admin、Chat、Knowledge）
│   ├── ai/                      # AI 助手编排、意图识别、提示词模板、RAG 知识库、工具调用与审计
│   ├── service/                 # 业务逻辑层（Menu、Order、Admin、User、Chat、Payment、Notification 等）
│   ├── mapper/                  # MyBatis-Plus BaseMapper 接口
│   ├── domain/                  # 数据库实体（Category、Dish、CustomerOrder、OrderItem、User、Review、OrderStatusLog）
│   ├── dto/                     # 请求/响应 DTO，含 Jakarta Validation
│   ├── security/                # JWT 工具类与认证过滤器
│   └── enums/                   # OrderStatus 状态机枚举
├── src/main/resources/
│   ├── application.yml          # 主配置（port、datasource、Redis、Milvus、AI、CORS、JWT）
│   ├── application-dev.yml      # 开发环境配置
│   ├── application-prod.yml     # 生产环境配置
│   ├── schema.sql               # 数据库建表 DDL
│   ├── data.sql                 # 初始种子数据
│   ├── prompts/                 # AI 提示词模板（.st 文件）
│   └── knowledge/               # 知识库 Markdown 文档
├── src/test/java/com/unbags/ordering/
│   ├── controller/              # Controller 层测试
│   ├── service/                 # Service 层测试
│   └── ai/                      # AI 模块测试
├── compose.yml                  # Docker Compose（Redis Stack + Milvus + etcd + MinIO）
└── pom.xml                      # Maven 依赖与构建配置
```

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis Stack 7.4+
- Milvus 2.5+（可通过 `compose.yml` 启动）

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动依赖服务

```bash
docker compose up -d
```

| 服务 | 端口 | 说明 |
|------|------|------|
| Redis Stack | 6379 | 菜单缓存 |
| Milvus | 19530 | 向量检索 |
| Milvus HTTP | 9091 | 健康检查 |
| MinIO Console | 9001 | 对象存储控制台 |

### 3. 配置环境变量

```bash
export DEEPSEEK_API_KEY=你的密钥
export DASHSCOPE_API_KEY=你的密钥
export JWT_SECRET=至少 32 字符的生产密钥
```

### 4. 启动服务

```bash
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`。

## 多环境配置

| Profile | SQL 初始化 | Milvus Schema | 数据库连接池 | 适用场景 |
|---------|-----------|---------------|-------------|----------|
| `dev` | 不自动执行 | 允许初始化 | 默认 | 本地开发 |
| `prod` | 不自动执行 | 跳过初始化 | HikariCP max 200, min-idle 30 | 生产部署 |

通过 `--spring.profiles.active=prod` 切换。

## API 接口

### 顾客端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/menu` | 获取完整菜单（分类 + 菜品） |
| POST | `/api/orders` | 创建订单（堂食/外带） |
| GET | `/api/orders/{orderNo}` | 查询订单详情与状态 |
| GET | `/api/cart` | 查询当前购物车 |
| POST | `/api/cart/items` | 加入购物车 |
| PUT | `/api/cart/items/{dishId}` | 修改购物车商品数量 |
| DELETE | `/api/cart/items/{dishId}` | 移除购物车商品 |
| DELETE | `/api/cart` | 清空购物车 |
| POST | `/api/chat/query` | AI 助手同步问答 |
| POST | `/api/chat/stream` | AI 助手 SSE 流式问答 |
| POST | `/api/reviews` | 提交菜品评价 |
| GET | `/api/reviews/dish/{dishId}` | 查询菜品评价 |
| GET | `/api/reviews/order/{orderNo}` | 查询订单评价 |

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录，返回 JWT |
| POST | `/api/auth/register` | 首次注册管理员 |
| GET | `/api/auth/status` | 查询系统初始化状态 |

### 管理端接口（需 JWT 认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 经营数据概览 |
| GET | `/api/admin/dashboard/product-sales` | 菜品销量排行 |
| GET | `/api/admin/orders` | 分页查询订单列表 |
| GET | `/api/admin/orders/{orderNo}` | 订单详情 |
| PATCH | `/api/admin/orders/{orderNo}/status` | 变更订单状态 |
| GET | `/api/admin/categories` | 分类列表 |
| POST | `/api/admin/categories` | 新增分类 |
| PUT | `/api/admin/categories/{categoryId}` | 更新分类 |
| DELETE | `/api/admin/categories/{categoryId}` | 删除分类 |
| GET | `/api/admin/dishes` | 菜品列表 |
| POST | `/api/admin/dishes` | 新增菜品 |
| PUT | `/api/admin/dishes/{dishId}` | 更新菜品 |
| PATCH | `/api/admin/dishes/{dishId}/availability` | 切换上下架 |
| DELETE | `/api/admin/dishes/{dishId}` | 删除菜品 |
| POST | `/api/admin/dishes/upload` | 上传菜品图片 |
| POST | `/api/admin/knowledge/upload` | 上传知识库文件 |
| POST | `/api/admin/knowledge/rebuild` | 手动重建菜单与客服知识库 |

## 核心特性

- **JWT 认证**：HMAC-SHA512 签名，无状态令牌，管理端接口和 WebSocket 连接需认证
- **WebSocket 推送**：STOMP 协议，订单状态变更和看板刷新实时通知管理端
- **Redis 菜单缓存**：高频菜单读走缓存，后台变更后主动失效
- **AI 点餐助手**：支持意图识别、商品解析、加购、推荐、订单查询、RAG 咨询和 SSE 流式回复，异常自动降级到规则回复
- **Milvus 知识库**：菜单 + 业务规则写入向量库，支持 RAG 语义检索增强问答；商品或分类变更后会刷新菜单缓存并重建知识库
- **AI 工具审计**：`ToolAuditSupport` 包装菜单、购物车、订单、业务规则和推荐工具调用，按配置记录成功/失败、参数摘要和耗时
- **接口中文化**：成功提示、参数校验错误、业务异常统一返回中文
- **Snowflake ID**：分布式唯一订单号，基于时间戳 + 工作机器 ID + 序列号
- **并发控制**：Semaphore 限流订单写入（默认 300 并发），公平队列
- **逻辑删除**：菜品使用 MyBatis-Plus @TableLogic 软删除
- **Prometheus 监控**：暴露 `/actuator/prometheus`
- **CORS 配置**：通过 `app.cors.allowed-origins` 控制允许的前端域名

## AI 配置

| 环境变量 | 默认值 | 说明 |
|------|--------|------|
| `DEEPSEEK_API_KEY` | 空 | DeepSeek API 密钥 |
| `DASHSCOPE_API_KEY` | 空 | DashScope embedding API 密钥 |
| `app.ai.enabled` | `true` | 启用 AI 对话 |
| `app.ai.rag.enabled` | `true` | 启用 RAG 检索 |
| `app.ai.tools.enabled` | `true` | 启用 Function Calling |
| `app.ai.tools.audit-enabled` | `true` | 启用 AI 工具调用审计日志 |
| `app.ai.rag.top-k` | `5` | 向量检索返回数 |
| `app.ai.rag.similarity-threshold` | `0.65` | 相似度阈值 |

## 数据库初始化

应用启动时由 `DatabaseSchemaInitializer` 创建和补齐基础表、列与索引。`schema.sql` 和 `data.sql` 保留为人工初始化或迁移参考；当前 `dev` 和 `prod` profile 都配置 `spring.sql.init.mode=never`，不会自动执行 SQL 文件。

## 构建与测试

```bash
# 编译检查
mvn -DskipTests compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests
```

## 生产部署

```bash
mvn clean package -DskipTests
java -jar target/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境必需的环境变量：

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_strong_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_very_long_secure_random_string
export DEEPSEEK_API_KEY=your_deepseek_key
export DASHSCOPE_API_KEY=your_dashscope_key
```