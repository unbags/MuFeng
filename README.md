# 沐枫餐饮点餐系统

沐枫餐饮点餐系统是一套面向餐饮门店的自用点餐与运营管理系统，包含顾客点餐端、店员管理端和 Spring Boot 后端服务。系统支持菜单浏览、下单、订单状态跟踪、后台商品与分类管理、订单处理、评价管理、实时推送、菜单缓存，以及可选的 AI 点餐助手和知识库检索能力。

本文档以当前后端架构和接口为主，说明系统组成、核心能力、启动方式、接口清单和部署配置。

## 项目结构

```text
MuFeng/
├── backend/          # Spring Boot 后端服务，提供业务接口、认证、缓存、实时推送和 AI 能力
├── frontend/         # Vue 3 店员管理端，提供登录、工作台、看板和商品管理页面
├── metaFront/        # Vue 3 顾客点餐端，提供菜单浏览、下单和订单跟踪页面
└── docs/             # 项目文档与过程记录
```

## 后端技术栈

| 类型 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 3.5.14 | 后端应用主体 |
| 数据访问 | MyBatis-Plus 3.5.7 | 实体映射、条件查询、逻辑删除 |
| 数据库 | MySQL 8.0+ | 存储菜单、订单、用户、评价等业务数据 |
| 缓存 | Redis Stack 7.4.0-v8 | 菜单缓存与高频读取加速 |
| 向量检索 | Milvus 2.5.10 | AI 知识库向量存储与检索 |
| 向量依赖 | etcd、MinIO | Milvus 元数据与对象存储 |
| 认证授权 | Spring Security、JWT | 管理端登录认证与令牌校验 |
| 实时通信 | WebSocket | 订单状态与看板数据推送 |
| AI 能力 | Spring AI、Spring AI Alibaba | DeepSeek 兼容对话、DashScope embedding、工具调用、RAG |
| 文档解析 | Apache Tika | 知识库文件解析与切片 |
| 监控 | Actuator、Prometheus | 健康检查与指标暴露 |
| 构建 | Maven、JDK 17 | 后端构建与运行环境 |

## 后端分层架构

```text
backend/src/main/java/com/example/ordering/
├── controller/       # REST 接口层，统一返回中文接口提示
├── service/          # 业务服务层，承载菜单、订单、用户、评价、文件解析等业务逻辑
├── mapper/           # MyBatis-Plus 数据访问层
├── domain/           # 数据库实体对象
├── dto/              # 接口请求与响应对象
├── mapping/          # DTO 与实体转换
├── config/           # 安全、跨域、Redis、WebSocket、限流、数据库初始化等配置
├── security/         # JWT 工具与认证过滤器
├── enums/            # 订单状态枚举与流转规则
└── ai/               # AI 助手、提示词模板、RAG 知识库、工具调用与审计
```

后端接口层只负责请求接入、参数校验和响应包装；核心业务由 Service 层完成；Mapper 层负责数据库访问；AI 模块通过可配置方式接入模型、工具和知识库，默认关闭，不影响基础点餐链路。

## 后端核心能力

- **菜单查询与缓存**：顾客端菜单优先读取 Redis 缓存，后台商品或分类变更后主动失效缓存。
- **订单创建与状态流转**：支持堂食和外带订单，生成订单号、计算费用、扣减库存、写入明细，并校验后台状态流转。
- **后台运营管理**：支持分类、菜品、图片、订单、看板、销量排行等管理接口。
- **管理员初始化与登录**：首次注册管理员后使用 JWT 登录，后续接口按安全配置校验。
- **评价管理**：支持按菜品和订单查询评价，支持顾客提交评价。
- **实时推送**：新订单、订单状态变更和看板刷新通过 WebSocket 推送给管理端。
- **高并发保护**：订单写入使用信号量限流，雪花算法生成全局唯一编号。
- **接口中文化**：默认成功提示、参数校验、业务异常、AI 知识库上传等接口提示均使用中文。
- **中文注释维护**：Controller、核心 Service、AI 工具和知识库方法已补充中文说明。
- **AI 点餐助手**：提供规则兜底、模型问答、流式回复、菜单知识库检索和工具调用入口。
- **知识库上传**：后台可上传文件到知识库，支持解析、切片并写入 Milvus。
- **监控与健康检查**：暴露健康检查和 Prometheus 指标。

## 后端依赖服务

后端开发环境依赖 MySQL、Redis Stack、Milvus、etcd 和 MinIO。仓库内 `backend/compose.yml` 可启动 Redis Stack 与 Milvus 相关依赖。

| 服务 | 默认端口 | 用途 |
|------|----------|------|
| MySQL | `3306` | 业务数据存储 |
| Redis Stack | `6379` | 菜单缓存 |
| Milvus | `19530` | 向量检索服务 |
| Milvus HTTP | `9091` | Milvus 健康检查 |
| MinIO Console | `9001` | Milvus 对象存储控制台 |

启动依赖服务：

```bash
docker compose -f backend/compose.yml up -d
```

检查容器状态：

```bash
docker compose -f backend/compose.yml ps
```

## 快速启动

### 1. 创建数据库

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认监听：

```text
http://localhost:8080
```

默认配置下，AI 模型调用、RAG 和工具调用均处于关闭状态。未配置 DeepSeek、DashScope 或 Milvus 密钥时，菜单、订单、后台管理等基础接口仍可正常运行。

### 3. 启动店员管理端

```bash
cd frontend
npm install
npm run dev
```

访问地址：

```text
http://localhost:7777
```

首次使用需要注册管理员账号。

### 4. 启动顾客点餐端

```bash
cd metaFront
npm install
npm run dev
```

访问地址：

```text
http://localhost:8888
```

两个前端均通过开发代理访问后端 `/api/**` 接口。

## 后端接口响应规范

普通接口统一返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

错误响应示例：

```json
{
  "code": 400,
  "message": "订单不存在"
}
```

参数校验失败、业务异常、服务不可用异常和未知异常由全局异常处理器统一转换为中文提示。

## 顾客端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/menu` | 查询完整菜单，包含分类和当前可售菜品 |
| `POST` | `/api/orders` | 创建订单，支持堂食和外带 |
| `GET` | `/api/orders/{orderNo}` | 根据订单号查询订单详情和状态 |
| `POST` | `/api/chat/query` | 智能助手一次性问答 |
| `POST` | `/api/chat/stream` | 智能助手流式问答，返回服务端事件流 |
| `POST` | `/api/reviews` | 提交菜品评价 |
| `GET` | `/api/reviews/dish/{dishId}` | 查询指定菜品评价 |
| `GET` | `/api/reviews/order/{orderNo}` | 查询指定订单评价 |

## 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/auth/login` | 管理员登录，成功后返回 JWT |
| `POST` | `/api/auth/register` | 系统首次初始化时注册管理员 |
| `GET` | `/api/auth/status` | 查询系统是否已经存在管理员 |

## 管理端接口

### 看板与订单

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/dashboard` | 查询后台经营数据概览 |
| `GET` | `/api/admin/dashboard/product-sales` | 查询指定时间范围内的菜品销量排行 |
| `GET` | `/api/admin/orders` | 分页查询订单列表，支持状态、类型、桌号和关键词筛选 |
| `GET` | `/api/admin/orders/{orderNo}` | 查询订单详情 |
| `PATCH` | `/api/admin/orders/{orderNo}/status` | 更新订单状态并记录状态日志 |

订单状态流转由后端枚举控制，避免非法跳转。取消订单时可记录取消原因和操作人。

### 分类管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/categories` | 查询分类列表 |
| `POST` | `/api/admin/categories` | 创建分类 |
| `PUT` | `/api/admin/categories/{categoryId}` | 更新分类名称和排序 |
| `DELETE` | `/api/admin/categories/{categoryId}` | 删除未被有效菜品占用的分类 |

### 菜品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/dishes` | 查询后台菜品列表 |
| `POST` | `/api/admin/dishes` | 创建菜品 |
| `PUT` | `/api/admin/dishes/{dishId}` | 更新菜品资料 |
| `PATCH` | `/api/admin/dishes/{dishId}/availability` | 切换菜品上下架状态 |
| `DELETE` | `/api/admin/dishes/{dishId}` | 删除菜品 |
| `POST` | `/api/admin/dishes/upload` | 上传菜品图片，返回图片访问地址 |

图片上传会校验文件大小、扩展名、文件头和图片可读性。

### 知识库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/admin/knowledge/upload` | 上传知识库文件，解析后写入 Milvus |

知识库上传接口在存在 `VectorStore` Bean 时启用。支持文件类型包括 `pdf`、`docx`、`doc`、`md`、`txt`、`markdown`、`html`、`htm`、`csv`，单文件大小上限为 `10MB`。文件解析后会按固定长度切片，并写入向量库。

## AI 点餐助手

AI 模块位于 `backend/src/main/java/com/example/ordering/ai`，由以下部分组成：

| 模块 | 说明 |
|------|------|
| `assistant` | 判断助手模式、构造用户消息、调用模型或规则兜底 |
| `prompt` | 读取并渲染中文提示词模板 |
| `rag` | 构建菜单知识文档、写入向量库、启动时刷新知识库 |
| `tools` | 提供菜单查询、菜品搜索、订单状态、推荐和业务规则工具 |

AI 模式由配置控制：

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `APP_AI_ENABLED` | `false` | 是否启用模型调用 |
| `APP_AI_RAG_ENABLED` | `false` | 是否启用知识库检索 |
| `APP_AI_TOOLS_ENABLED` | `false` | 是否启用工具调用 |
| `APP_AI_TOOLS_AUDIT_ENABLED` | `true` | 是否记录工具调用审计 |
| `APP_AI_PROMPT_LOCATION` | `classpath:/prompts` | 提示词模板目录 |

启用示例：

```bash
export APP_AI_ENABLED=true
export APP_AI_RAG_ENABLED=true
export APP_AI_TOOLS_ENABLED=true
export SPRING_AI_MODEL_CHAT=deepseek
export SPRING_AI_MODEL_EMBEDDING=dashscope
export SPRING_AI_VECTORSTORE_TYPE=milvus
export SPRING_AI_VECTORSTORE_MILVUS_INITIALIZE_SCHEMA=true
export DEEPSEEK_API_KEY=你的密钥
export DASHSCOPE_API_KEY=你的密钥
```

## 关键配置

常用环境变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | `root` | MySQL 用户名 |
| `DB_PASSWORD` | `123456` | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `MILVUS_HOST` | `localhost` | Milvus 主机 |
| `MILVUS_PORT` | `19530` | Milvus 端口 |
| `MILVUS_COLLECTION_NAME` | `mufeng_kb` | Milvus 集合名称 |
| `JWT_SECRET` | 开发默认密钥 | JWT 签名密钥，生产环境必须替换 |
| `DEEPSEEK_API_KEY` | 空 | DeepSeek API 密钥 |
| `DASHSCOPE_API_KEY` | 空 | DashScope API 密钥 |

生产环境至少需要替换数据库账号密码、Redis 地址密码和 JWT 密钥。

## 页面路由

### 店员管理端

| 路径 | 说明 |
|------|------|
| `/login` | 管理员登录 |
| `/register` | 首次初始化管理员账号 |
| `/workbench` | 点餐工作台 |
| `/dashboard` | 数据看板 |
| `/products` | 商品管理 |

### 顾客点餐端

| 路径 | 说明 |
|------|------|
| `/menu` | 菜品菜单浏览 |
| `/menu/:id` | 菜品详情 |
| `/orders/:orderNo` | 订单状态跟踪 |

## 验证与构建

后端编译：

```bash
cd backend
mvn -DskipTests compile
```

运行单元测试：

```bash
cd backend
mvn test
```

只验证接口中文提示回归测试：

```bash
cd backend
mvn -Dtest=UserFacingMessageLocalizationTest test
```

如果本机没有 JDK 或 Maven，可使用本地 Maven 容器：

```bash
docker run --rm -v "$PWD":/workspace -w /workspace/backend maven:3.9.9-eclipse-temurin-17 mvn -q -DskipTests compile
docker run --rm -v "$PWD":/workspace -w /workspace/backend maven:3.9.9-eclipse-temurin-17 mvn -q -Dtest=UserFacingMessageLocalizationTest test
```

若在容器中运行完整测试遇到 Mockito 内联模拟器无法附加 Java agent 的问题，建议改用本机 JDK 17 与 Maven 运行完整测试；该问题属于容器运行环境限制，不影响后端主代码编译。

## 生产部署建议

后端建议打包为 Jar 后通过 systemd、Supervisor 或容器平台托管：

```bash
cd backend
mvn clean package -DskipTests
java -jar target/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境建议：

- 使用独立 MySQL、Redis、Milvus 集群或托管服务。
- 使用 Nginx 或负载均衡器转发前端静态资源和后端接口。
- 替换 `JWT_SECRET`，并管理好数据库与模型服务密钥。
- 根据实际并发量调整 Tomcat、HikariCP、Redis 连接池和订单写入限流参数。
- 接入 Prometheus 采集 `/actuator/prometheus` 指标。
- 针对高并发下单场景，后续可接入消息队列、幂等键、读写分离和分库分表。

## 监控端点

| 路径 | 说明 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/prometheus` | Prometheus 指标 |

## 许可证

详见 [LICENSE](LICENSE)。
