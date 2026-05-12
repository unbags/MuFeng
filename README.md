# 沐枫餐饮在线点餐系统

沐枫餐饮在线点餐系统是一套面向餐饮门店的线上点餐与运营管理平台，包含顾客点餐端、店员管理端和 Spring Boot 后端服务。系统支持菜单浏览、在线下单、订单状态跟踪、评价管理、后台商品与分类管理、订单处理、实时推送、菜单缓存，以及可配置的 AI 点餐助手和知识库检索。

## 项目结构

```text
MuFeng/
├── backend/          # Spring Boot 后端服务，REST API、认证、缓存、WebSocket、AI
├── frontend/         # Vue 3 店员管理端，登录、点餐工作台、数据看板、商品管理、知识库
├── metaFront/        # Vue 3 顾客端，菜单浏览、在线下单、订单跟踪、AI 客服
├── LICENSE
└── README.md
```

## 后端技术栈

| 类型 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 3.5.14 | 后端应用主体 |
| 数据访问 | MyBatis-Plus 3.5.7 | 实体映射、条件查询、逻辑删除 |
| 数据库 | MySQL 8.0+ | 存储菜单、订单、用户、评价等业务数据 |
| 缓存 | Redis Stack 7.4 | 菜单缓存与高频读取加速 |
| 向量检索 | Milvus 2.5 | AI 知识库向量存储与语义检索 |
| 认证授权 | Spring Security + JWT 0.12.6 | HMAC-SHA512 无状态令牌认证 |
| 实时推送 | WebSocket + STOMP | 订单状态与看板数据实时推送 |
| AI 对话 | Spring AI 1.1.6 | DeepSeek 兼容对话、RAG 检索、工具调用 |
| AI 嵌入 | Spring AI Alibaba 1.1.2.2 | DashScope text-embedding-v4 (1024维) |
| 文档解析 | Apache Tika 2.9.2 | 知识库文件文本提取与切片 |
| 监控 | Actuator + Prometheus | 健康检查与指标暴露 |
| 并发控制 | Semaphore + Snowflake | 订单写入限流与全局唯一 ID |
| 构建 | Maven + JDK 17 | 后端构建与运行环境 |

## 前端技术栈

| 类型 | 顾客端 (metaFront) | 管理端 (frontend) |
|------|-------------------|-------------------|
| 框架 | Vue 3.5 (Composition API) | Vue 3.5 (Composition API) |
| 路由 | Vue Router 4.6 (History 模式) | Vue Router 4.6 (Hash 模式) |
| 构建 | Vite 6.3 | Vite 5.4 |
| 开发端口 | 8888 | 7777 |
| 认证 | 无（公开访问） | JWT 令牌认证 |
| 样式 | 毛玻璃卡片 + 暖金色调 | Apple 风格暖色调设计系统 |

## 后端分层架构

```text
backend/src/main/java/com/example/ordering/
├── controller/       # REST 接口层，统一返回 { code, message, data } 格式
├── service/          # 业务服务层，菜单、订单、用户、评价、通知、支付、文档解析
├── mapper/           # MyBatis-Plus 数据访问层，含自定义库存扣减与统计查询
├── domain/           # 数据库实体（Category, Dish, CustomerOrder, OrderItem, User, Review, OrderStatusLog）
├── dto/              # 请求/响应 DTO，含参数校验与中文提示
├── config/           # Security、CORS、Redis、WebSocket、MyBatis-Plus、限流、异常处理等配置
├── security/         # JWT 工具类与 OncePerRequestFilter 认证过滤器
├── enums/            # OrderStatus 枚举与状态机流转规则
└── ai/               # AI 助手编排、提示词模板渲染、RAG 知识库构建/写入/刷新、工具调用与审计
```

## 后端核心能力

- **菜单查询与缓存**：顾客端菜单优先读取 Redis，后台变更后主动失效缓存，TTL 可配置
- **在线下单**：支持堂食（填桌号）和外带（到店自取），生成唯一订单号、实时计价、原子扣减库存
- **订单状态流转**：PENDING → PREPARING → COMPLETED / CANCELLED，由状态机校验防止非法跳转
- **后台运营管理**：菜品 CRUD、分类管理、图片上传、订单查询与状态变更、数据看板、菜品销量排行
- **管理员认证**：首次注册管理员后使用 JWT 登录，管理端接口和 WebSocket 连接需认证
- **评价管理**：顾客提交菜品评分，支持按菜品和订单查询评价
- **实时推送**：新订单、状态变更、看板刷新通过 STOMP WebSocket 推送到管理端
- **高并发保护**：Semaphore 限流订单写入 (默认 300 并发)，Snowflake 算法生成全局唯一 ID
- **AI 点餐助手**：可配置四种模式（规则兜底 / LLM 对话 / RAG 检索 / 全能力），支持 SSE 流式回复
- **知识库管理**：后台可上传 PDF/DOCX/MD/TXT 等文件，自动解析切片写入 Milvus 向量库
- **监控与健康检查**：暴露 `/actuator/health` 和 `/actuator/prometheus`

## 后端依赖服务

| 服务 | 端口 | 用途 |
|------|------|------|
| MySQL | 3306 | 业务数据存储 |
| Redis Stack | 6379 | 菜单缓存 |
| Milvus | 19530 | 向量检索 |
| Milvus HTTP | 9091 | Milvus 健康检查 |
| MinIO Console | 9001 | Milvus 对象存储控制台 |

启动开发环境依赖：

```bash
docker compose -f backend/compose.yml up -d
```

## 快速启动

### 1. 创建数据库

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端

```bash
cd backend
# 配置环境变量
export DEEPSEEK_API_KEY=你的密钥
export DASHSCOPE_API_KEY=你的密钥
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080`。

### 3. 启动店员管理端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:7777`，首次使用需注册管理员账号。

### 4. 启动顾客端

```bash
cd metaFront
npm install
npm run dev
```

访问 `http://localhost:8888`。

## 接口规范

所有接口统一返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

参数校验失败、业务异常、系统异常由全局异常处理器统一转换为中文提示。

## 顾客端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/menu` | 查询完整菜单（分类 + 菜品） |
| `POST` | `/api/orders` | 创建订单（堂食/外带） |
| `GET` | `/api/orders/{orderNo}` | 查询订单详情与状态 |
| `POST` | `/api/chat/query` | AI 助手同步问答 |
| `POST` | `/api/chat/stream` | AI 助手 SSE 流式问答 |
| `POST` | `/api/reviews` | 提交菜品评价 |
| `GET` | `/api/reviews/dish/{dishId}` | 查询菜品评价 |
| `GET` | `/api/reviews/order/{orderNo}` | 查询订单评价 |

## 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/auth/login` | 管理员登录，返回 JWT |
| `POST` | `/api/auth/register` | 系统首次初始化注册管理员 |
| `GET` | `/api/auth/status` | 查询系统是否已有管理员 |

## 管理端接口（需认证）

### 看板

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/dashboard` | 经营数据概览（营收、订单数、菜品数） |
| `GET` | `/api/admin/dashboard/product-sales` | 菜品销量排行（支持 week/month/year） |

### 订单管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/orders` | 分页查询订单，支持状态/类型/桌号/关键词筛选 |
| `GET` | `/api/admin/orders/{orderNo}` | 查询订单完整详情 |
| `PATCH` | `/api/admin/orders/{orderNo}/status` | 变更订单状态，记录操作日志 |

### 分类管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/categories` | 分类列表 |
| `POST` | `/api/admin/categories` | 新增分类 |
| `PUT` | `/api/admin/categories/{categoryId}` | 更新分类 |
| `DELETE` | `/api/admin/categories/{categoryId}` | 删除分类（仅限无菜品的分类） |

### 菜品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/dishes` | 菜品列表 |
| `POST` | `/api/admin/dishes` | 新增菜品 |
| `PUT` | `/api/admin/dishes/{dishId}` | 更新菜品 |
| `PATCH` | `/api/admin/dishes/{dishId}/availability` | 切换上下架 |
| `DELETE` | `/api/admin/dishes/{dishId}` | 删除菜品（逻辑删除） |
| `POST` | `/api/admin/dishes/upload` | 上传菜品图片 |

### 知识库

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/admin/knowledge/upload` | 上传文件写入向量知识库 |

## AI 点餐助手

AI 模块由四部分组成：

| 模块 | 路径 | 说明 |
|------|------|------|
| `assistant` | `ai/assistant/` | 模式判断、用户消息构造、模型调用与规则兜底降级 |
| `prompt` | `ai/prompt/` | 加载 `.st` 模板，渲染中文提示词 |
| `rag` | `ai/rag/` | 菜单知识文档构建、Milvus 写入、启动时刷新索引 |
| `tools` | `ai/tools/` | 菜单查询、菜品搜索、订单状态、推荐、业务规则工具 |

配置项：

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `app.ai.enabled` | `true` | 启用 AI 对话 |
| `app.ai.rag.enabled` | `true` | 启用 RAG 知识库检索 |
| `app.ai.tools.enabled` | `false` | 启用 Function Calling 工具 |
| `app.ai.rag.top-k` | `5` | 向量检索返回文档数 |
| `app.ai.rag.similarity-threshold` | `0.65` | 相似度阈值 |

## 页面路由

### 店员管理端 (frontend)

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | LoginView | 管理员登录 |
| `/register` | RegisterView | 首次注册管理员 |
| `/workbench` | CustomerView | 点餐工作台（为到店顾客下单） |
| `/dashboard` | DashboardView | 数据看板 |
| `/products` | AdminView | 菜品与分类管理 |
| `/knowledge` | KnowledgeView | 知识库文件上传 |

### 顾客端 (metaFront)

| 路径 | 页面 | 说明 |
|------|------|------|
| `/menu` | CollectionView | 菜单浏览与搜索 |
| `/menu/:id` | ProductDetailView | 菜品详情 |
| `/orders/:orderNo` | OrderStatusView | 订单状态实时跟踪 |

## 关键配置

| 环境变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | `root` | MySQL 用户名 |
| `DB_PASSWORD` | `123456` | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `JWT_SECRET` | 开发默认值 | JWT HMAC-SHA512 密钥，生产必须替换 |
| `DEEPSEEK_API_KEY` | 空 | DeepSeek API 密钥 |
| `DASHSCOPE_API_KEY` | 空 | DashScope embedding API 密钥 |

## 构建与测试

```bash
# 后端编译
cd backend
mvn -DskipTests compile

# 后端测试
mvn test

# 后端打包
mvn clean package -DskipTests
```

## 生产部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境建议：
- 使用独立 MySQL、Redis、Milvus 集群或托管服务
- Nginx 反向代理前端静态资源与后端 API，配置 WebSocket 升级
- 替换 `JWT_SECRET`，管理好数据库与模型服务密钥
- 根据并发量调整 Tomcat 线程池、HikariCP 连接池和订单写入限流参数
- 接入 Prometheus 采集 `/actuator/prometheus` 指标

## 监控端点

| 路径 | 说明 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/prometheus` | Prometheus 指标 |

## 许可证

详见 [LICENSE](LICENSE)。
