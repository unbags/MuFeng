# 登录与注册功能 - 设计方案

## 概述

为"暖光食刻"餐厅管理系统实现完整的管理员登录与注册功能。
当前系统只有硬编码的 `admin/admin123` 认证，前端无任何认证能力。

## 设计决策

| 决策项 | 选择 |
|--------|------|
| 用户角色 | 仅管理员（单一角色） |
| 注册策略 | 仅首次初始化 — 系统无管理员时允许注册，之后禁止 |
| UI 形式 | 独立全屏页面（`/login`、`/register`） |
| 登录页布局 | 左右分栏式（左侧品牌展示 + 右侧表单） |
| 密码策略 | 基础要求：最少 6 位，必须包含字母和数字 |
| 账号标识 | 用户名 |

## 架构

```
前端 Vue 3 (LoginView, RegisterView, useAuth, router guards, api token)
    │
    ▼ POST /api/auth/login | /register | GET /api/auth/status
后端 Spring Boot (AuthController, UserService, UserMapper, BCrypt)
    │
    ▼
数据库 MySQL (users 表)
```

## 数据库

新增 `users` 表：

```sql
CREATE TABLE users (
    id            BIGINT PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 后端

### 新增文件
- `domain/User.java` — MyBatis-Plus 实体
- `mapper/UserMapper.java` — 数据访问接口
- `service/UserService.java` — 注册校验、BCrypt 加密、用户名唯一性
- `service/impl/UserServiceImpl.java` — 实现类
- `dto/RegisterRequest.java` — 注册请求 DTO（username, password, displayName）
- `dto/AuthStatusResponse.java` — 初始化状态响应（hasAdmin: boolean）

### 修改文件
- `AuthController.java`:
  - `POST /api/auth/login` — 改为查数据库 + BCrypt 验证（不再读 application.yml）
  - `POST /api/auth/register` — 新增，仅首次初始化可用
  - `GET /api/auth/status` — 新增，返回是否已有管理员
- `SecurityConfig.java` — `/api/auth/register`、`/api/auth/status` 加入 permitAll
- `application.yml` — 移除 `app.admin.username/password`（或标记为 deprecated 回退）

### API 规格

**POST /api/auth/login**
```
Request:  { "username": "admin", "password": "abc123" }
Response: { "token": "eyJ...", "username": "admin", "displayName": "管理员" }
Errors:   401 用户名或密码错误
```

**POST /api/auth/register**
```
Request:  { "username": "admin", "password": "abc123", "displayName": "管理员" }
Response: { "token": "eyJ...", "username": "admin", "displayName": "管理员" }
Errors:   400 密码格式不符 / 用户名已存在
          403 系统已初始化，禁止注册
```

**GET /api/auth/status**
```
Response: { "hasAdmin": true }
```

## 前端

### 新增文件
- `views/LoginView.vue` — 左右分栏登录页
- `views/RegisterView.vue` — 左右分栏注册页（风格统一）
- `api/auth.js` — `login()`, `register()`, `fetchAuthStatus()`
- `composables/useAuth.js` — token 管理（localStorage）、状态、方法

### 修改文件
- `router/index.js`:
  - 新增路由: `/login` → LoginView, `/register` → RegisterView
  - `beforeEach` 守卫:
    - `/login`、`/register`: 已登录 → `/dashboard`
    - `/dashboard`、`/products`: 未登录 → `/login`
    - `/workbench`、`/`: 公开
- `api/index.js` — `request()` 自动附加 `Authorization: Bearer <token>`
- `components/layout/AppSidebar.vue` — 底部添加登出按钮

### 认证流程

```
App 启动 → router.beforeEach 检查 token
  ├── 无 token → 访问管理页 → 重定向 /login
  ├── 有 token → 访问 /login → 重定向 /dashboard
  └── 访问 /workbench → 始终放行

LoginView
  → 输入用户名密码 → api.login()
  → 成功: 存储 token, redirect /dashboard
  → 失败: 显示错误提示

RegisterView
  → 首次访问时: api.fetchAuthStatus() 检查 hasAdmin
  → hasAdmin=true: 显示"系统已初始化，请联系管理员"
  → hasAdmin=false: 显示注册表单
  → 注册成功: 存储 token, redirect /dashboard
```

## 文件清单

| 层级 | 操作 | 文件 |
|------|------|------|
| DB | 新增 | `schema.sql` — users 表 |
| 后端 | 新增 | `domain/User.java` |
| 后端 | 新增 | `mapper/UserMapper.java` |
| 后端 | 新增 | `service/UserService.java` |
| 后端 | 新增 | `service/impl/UserServiceImpl.java` |
| 后端 | 新增 | `dto/RegisterRequest.java` |
| 后端 | 新增 | `dto/AuthStatusResponse.java` |
| 后端 | 修改 | `controller/AuthController.java` |
| 后端 | 修改 | `config/SecurityConfig.java` |
| 后端 | 修改 | `application.yml` |
| 前端 | 新增 | `views/LoginView.vue` |
| 前端 | 新增 | `views/RegisterView.vue` |
| 前端 | 新增 | `api/auth.js` |
| 前端 | 新增 | `composables/useAuth.js` |
| 前端 | 修改 | `router/index.js` |
| 前端 | 修改 | `api/index.js` |
| 前端 | 修改 | `components/layout/AppSidebar.vue` |
