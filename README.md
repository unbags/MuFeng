# 沐枫餐饮 (MuFeng)

自用点餐系统，支持顾客扫码点餐与管理端后台运营（功能正在完善中）。

## 项目结构

```
MuFeng/
├── backend/          # Spring Boot 后端服务
├── frontend/         # Vue 3 店员管理端（点餐工作台、数据看板、商品管理）
└── metaFront/        # Vue 3 顾客点餐端（菜单浏览、下单、订单跟踪）
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.14 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0+ |
| 缓存/向量库 | Redis Stack | 7.4.0-v8 |
| AI 前置能力 | Spring AI / Spring AI Alibaba | 1.1.2 / 1.1.2.2 |
| 认证 | Spring Security + JWT | - |
| 实时通信 | WebSocket | - |
| 前端框架 | Vue 3 (Composition API) | 3.5 |
| 前端路由 | Vue Router | 4.6 |
| 构建工具 | Vite | 5.4 / 6.3 |

## 环境要求

- **后端**：JDK 17+ / Maven 3.9+ / MySQL 8.0+ / Redis Stack 7.4+
- **前端**：Node.js 18+

## 快速开始

### 1. 环境准备

创建数据库：

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

启动 Redis Stack：

```bash
docker compose -f backend/compose.yml up -d
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`。

默认情况下 AI 自动配置处于关闭状态，未配置 DeepSeek 或 DashScope 密钥不会影响菜单、订单、管理端等现有接口启动。

### 3. 启动前端

**店员管理端（frontend）—— 端口 7777：**

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:7777`，首次使用需先注册管理员账号。

**顾客点餐端（metaFront）—— 端口 8888：**

```bash
cd metaFront
npm install
npm run dev
```

访问 `http://localhost:8888`，顾客扫码后浏览菜单并下单。

两个前端均内置 API 代理，`/api/*` 请求自动转发到后端 `localhost:8080`，无需额外配置 CORS。

## 核心特性

- **JWT 无状态认证**：支持 token 过期与刷新
- **WebSocket 实时推送**：订单状态变更即时通知管理端
- **Redis 菜单缓存**：高频读取走缓存，TTL 可配置
- **AI 前置依赖**：已预留 DeepSeek 对话、DashScope embedding、Redis Stack 向量库配置入口
- **雪花算法 ID**：分布式唯一订单号
- **高并发控制**：订单写入限流
- **逻辑删除**：数据软删除，可追溯
- **Prometheus 监控**：暴露 `/actuator/prometheus` 端点

## API 概览

### 客户端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/menu` | 获取完整菜单（分类+菜品） |
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{orderNo}` | 查询订单状态 |

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录 |
| POST | `/api/auth/register` | 管理员注册（首次初始化） |
| GET | `/api/auth/status` | 系统初始化状态检查 |

### 管理端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘数据 |
| GET | `/api/admin/orders` | 订单列表 |
| PATCH | `/api/admin/orders/{orderNo}/status` | 更新订单状态 |
| CRUD | `/api/admin/categories` | 分类管理 |
| CRUD | `/api/admin/dishes` | 菜品管理 |

## 页面路由

### 店员管理端（frontend :7777）

| 路径 | 说明 |
|------|------|
| `/login` | 管理员登录 |
| `/register` | 首次初始化管理员账号 |
| `/workbench` | 点餐工作台 |
| `/dashboard` | 数据看板 |
| `/products` | 商品管理 |

### 顾客点餐端（metaFront :8888）

| 路径 | 说明 |
|------|------|
| `/menu` | 菜品菜单浏览 |
| `/menu/:id` | 菜品详情 |
| `/orders/:orderNo` | 订单状态跟踪 |

## 生产部署

后端推荐使用 systemd 管理服务，前端推荐 Nginx 静态部署 + 反向代理。详细配置参见 [backend/README.md](backend/README.md)、[frontend/README.md](frontend/README.md) 和 [metaFront/README.md](metaFront/README.md)。

### 环境变量

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_strong_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export DEEPSEEK_API_KEY=your_deepseek_api_key
export DASHSCOPE_API_KEY=your_dashscope_api_key
export JWT_SECRET=your_secure_random_string
```

### AI 能力开关

后端已完成 RAG/function call 的前置依赖升级，但默认不启用模型自动配置。后续接入智能体时可按需打开：

```bash
export SPRING_AI_MODEL_CHAT=openai
export SPRING_AI_MODEL_EMBEDDING=dashscope
export SPRING_AI_VECTORSTORE_REDIS_ENABLED=true
export SPRING_AI_VECTORSTORE_REDIS_INITIALIZE_SCHEMA=true
```

## 许可证

详见 [LICENSE](LICENSE)
