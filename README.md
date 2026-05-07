# 沐风餐饮 (MuFeng)

自用点餐系统，支持顾客扫码点餐与管理端后台运营。

## 项目结构

```
MuFeng/
├── backend/          # Spring Boot 后端服务
├── frontend/         # Vue 3 前端应用
└── docs/             # 项目文档
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 6.0+ |
| 认证 | Spring Security + JWT | - |
| 实时通信 | WebSocket | - |
| 前端框架 | Vue 3 (Composition API) | 3.5 |
| 前端路由 | Vue Router | 4.6 |
| 构建工具 | Vite | 5.4 |

## 环境要求

- **后端**：JDK 1.8+ / Maven 3.9+ / MySQL 8.0+ / Redis 6.0+
- **前端**：Node.js 18+

## 快速开始

### 1. 环境准备

创建数据库并启动 Redis：

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

开发服务器访问 `http://localhost:5173`，内置代理将 `/api/*` 转发到后端 `localhost:8080`。

## 核心特性

- **JWT 无状态认证**：支持 token 过期与刷新
- **WebSocket 实时推送**：订单状态变更即时通知管理端
- **Redis 菜单缓存**：高频读取走缓存，TTL 可配置
- **雪花算法 ID**：分布式唯一订单号
- **高并发控制**：订单写入限流
- **逻辑删除**：数据软删除，可追溯
- **Prometheus 监控**：暴露 `/actuator/prometheus` 端点

## API 概览

### 客户端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/menu` | 获取完整菜单 |
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{orderNo}` | 查询订单状态 |

### 管理端

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/admin/dashboard` | 仪表盘数据 |
| GET | `/api/admin/orders` | 订单列表 |
| PATCH | `/api/admin/orders/{orderNo}/status` | 更新订单状态 |
| CRUD | `/api/admin/categories` | 分类管理 |
| CRUD | `/api/admin/dishes` | 菜品管理 |

## 页面路由（前端）

| 路径 | 说明 |
|------|------|
| `/` | 顾客点餐页面 |
| `/admin` | 菜品/分类管理 |
| `/admin/dashboard` | 管理仪表盘 |
| `/login` | 管理员登录 |
| `/register` | 管理员注册 |

## 生产部署

后端推荐使用 systemd 管理服务，前端推荐 Nginx 静态部署 + 反向代理。详细配置参见 [backend/README.md](backend/README.md) 和 [frontend/README.md](frontend/README.md)。

### 环境变量

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_strong_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_secure_random_string
```

## 许可证

详见 [LICENSE](LICENSE)
