# 沐枫餐饮 - 后端服务

Spring Boot 后端服务，提供 REST API、JWT 认证、WebSocket 实时推送和 Redis 缓存。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.5.14 | 核心框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis Stack | 7.4+ | 缓存与向量知识库 |
| Spring Security | 3.5 | 认证与授权 |
| JWT | 0.12.6 | 无状态令牌认证 |
| WebSocket | 3.5 | 实时推送（订单状态通知等） |
| Spring AI | 1.1.2 | DeepSeek OpenAI 兼容对话入口与 Redis Vector Store |
| Spring AI Alibaba | 1.1.2.2 | DashScope embedding 与 agent workflow 前置依赖 |
| MapStruct | 1.5.5 | 对象映射转换 |
| Lombok | - | 简化 Java 代码 |
| Micrometer + Prometheus | - | 应用监控与指标采集 |
| Maven | 3.9 | 项目构建管理 |

## 项目结构

```
backend/
├── src/main/java/com/example/ordering/
│   ├── config/          # Spring 配置类（Security、CORS、WebSocket、限流等）
│   ├── controller/      # 控制器层
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis-Plus 数据访问层
│   ├── domain/          # 数据库实体
│   ├── dto/             # 数据传输对象
│   ├── mapping/         # MapStruct DTO 映射
│   └── security/        # JWT 认证过滤器与令牌工具
├── src/main/resources/
│   ├── application.yml       # 主配置文件
│   ├── application-dev.yml   # 开发环境配置
│   ├── application-prod.yml  # 生产环境配置
│   ├── schema.sql            # 数据库建表脚本
│   └── data.sql              # 初始种子数据
├── compose.yml               # Redis Stack 开发环境
└── pom.xml                   # Maven 依赖配置
```

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis Stack 7.4+

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE ordering_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动 Redis Stack

开发环境推荐使用仓库内 Compose 文件启动 Redis Stack：

```bash
docker compose up -d
```

服务默认监听 `localhost:6379`（无密码）。Redis Stack 提供后续 RAG 知识库所需的向量索引能力。

### 3. 修改配置（可选）

编辑 `src/main/resources/application.yml`，按需修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ordering_demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

常用环境变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | `root` | MySQL 用户名 |
| `DB_PASSWORD` | `123456` | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis Stack 主机 |
| `REDIS_PORT` | `6379` | Redis Stack 端口 |
| `REDIS_PASSWORD` | 空 | Redis Stack 密码 |
| `JWT_SECRET` | 开发默认密钥 | JWT HS512 签名密钥，生产必须替换 |
| `DEEPSEEK_API_KEY` | 空 | DeepSeek 官方 OpenAI 兼容 API Key |
| `DASHSCOPE_API_KEY` | 空 | DashScope embedding API Key |

### 4. 启动服务

```bash
# 开发环境（默认 profile）
mvn spring-boot:run

# 指定生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

服务启动后访问 `http://localhost:8080`。

## 多环境配置

| Profile | SQL 初始化 | Redis Vector Store | 数据库连接池 | 适用场景 |
|---------|-----------|--------------------|-------------|----------|
| `dev` | 不执行 | 默认允许 | 默认配置 | 本地开发 |
| `prod` | 禁用 | 固定关闭 | HikariCP 增强配置（最大 200 连接） | 生产部署 |

通过 `--spring.profiles.active=prod` 切换环境。

## API 接口

### 客户端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/menu` | 获取完整菜单（分类+菜品） |
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{orderNo}` | 查询订单状态 |

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录 |
| POST | `/api/auth/register` | 管理员注册（首次初始化） |
| GET | `/api/auth/status` | 检查系统初始化状态 |

### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘数据 |
| GET | `/api/admin/orders` | 订单列表 |
| GET | `/api/admin/orders/{orderNo}` | 订单详情 |
| PATCH | `/api/admin/orders/{orderNo}/status` | 更新订单状态 |
| GET | `/api/admin/categories` | 分类列表 |
| POST | `/api/admin/categories` | 新增分类 |
| PUT | `/api/admin/categories/{categoryId}` | 编辑分类 |
| DELETE | `/api/admin/categories/{categoryId}` | 删除分类 |
| GET | `/api/admin/dishes` | 菜品列表 |
| POST | `/api/admin/dishes` | 新增菜品 |
| PUT | `/api/admin/dishes/{dishId}` | 编辑菜品 |
| PATCH | `/api/admin/dishes/{dishId}/availability` | 切换菜品上下架 |
| DELETE | `/api/admin/dishes/{dishId}` | 删除菜品 |
| POST | `/api/admin/dishes/upload` | 上传菜品图片 |

## 核心特性

- **JWT 认证**：无状态登录，支持 token 过期与刷新
- **WebSocket 推送**：订单状态变更实时通知管理端
- **Redis 菜单缓存**：高频菜单读取走缓存，TTL 可配置
- **雪花算法 ID**：分布式唯一订单号生成
- **高并发控制**：订单写入限流（默认 300 并发）
- **逻辑删除**：数据软删除，可追溯
- **Prometheus 监控**：暴露 `/actuator/prometheus` 端点
- **CORS 配置**：通过 `app.cors.allowed-origins` 配置允许的前端域名

## 验证与构建

```bash
# 单元测试
mvn test

# 编译打包
mvn -DskipTests package

# 校验 Redis Stack Compose 配置
docker compose config
```

如果本机没有安装 JDK 17/Maven，可使用 Maven 容器验证：

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-17 mvn test
```

## 生产部署

### 方式一：Jar 包部署

```bash
# 打包
mvn clean package -DskipTests

# 启动
java -jar target/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 后台运行（推荐配合 systemd 或 supervisor）
nohup java -jar target/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### 方式二：systemd 服务（推荐）

创建 `/etc/systemd/system/ordering-backend.service`：

```ini
[Unit]
Description=沐枫餐饮后端服务
After=network.target mysql.service redis.service

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/ordering/backend
ExecStart=/usr/bin/java -jar /opt/ordering/backend/ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable ordering-backend
sudo systemctl start ordering-backend
```

### 生产环境必需的环境变量

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_strong_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_very_long_secure_random_string
```
