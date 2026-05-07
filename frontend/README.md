# 沐风餐饮 - 前端应用

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式前端框架（Composition API） |
| Vue Router | 4.6 | 官方路由管理器 |
| Vite | 5.4 | 新一代前端构建工具 |

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 请求模块（auth、menu、orders、dishes、categories、dashboard）
│   ├── components/        # 可复用组件
│   │   ├── admin/         # 管理端组件（菜品卡片、表单弹窗、图片上传等）
│   │   ├── cart/          # 购物车组件（面板、结算弹窗、小票弹窗）
│   │   ├── dashboard/     # 仪表盘组件（指标卡片、图表、订单类型分布）
│   │   ├── layout/        # 布局组件（侧边栏、Toast、Loading）
│   │   ├── menu/          # 菜单组件（菜品卡片、点餐类型切换）
│   │   └── ui/            # 通用 UI 组件（分页、数量控制、空状态、图片容错）
│   ├── composables/       # 组合式函数（状态管理、购物车、菜单、订单、认证等）
│   ├── config/            # 常量配置
│   ├── router/            # 路由定义
│   ├── styles/            # 样式文件（CSS Token、布局、组件、响应式、动画）
│   ├── utils/             # 工具函数（格式化、数据规范化）
│   ├── views/             # 页面视图（顾客端、管理端、登录、注册、仪表盘、404）
│   ├── App.vue            # 根组件
│   └── main.js            # 应用入口
├── index.html             # HTML 入口
├── vite.config.js         # Vite 配置
└── package.json           # 依赖与脚本
```

## 页面路由

| 路径 | 视图 | 说明 |
|------|------|------|
| `/` | CustomerView | 顾客点餐页面 |
| `/admin` | AdminView | 菜品/分类管理页面 |
| `/admin/dashboard` | DashboardView | 管理仪表盘 |
| `/login` | LoginView | 管理员登录 |
| `/register` | RegisterView | 管理员注册 |
| `/:pathMatch(.*)*` | NotFoundView | 404 页面 |

## 环境要求

- Node.js 18+
- npm 或 pnpm

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

默认访问 `http://localhost:5173`。

开发服务器内置了 API 代理：所有 `/api/*` 请求会被转发到 `http://localhost:8080`（后端服务）。无需额外配置 CORS。

### 3. 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/` 目录。

### 4. 预览生产版本

```bash
npm run preview
```

## 生产部署

### 方式一：Nginx 静态部署（推荐）

构建后将 `dist/` 目录部署到服务器，使用 Nginx 做静态文件服务 + 反向代理：

```nginx
server {
    listen       80;
    server_name  your-domain.com;

    # 静态资源
    root   /opt/ordering/frontend/dist;
    index  index.html;

    # Vue Router history 模式回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
    }

    # WebSocket 代理（订单状态实时推送）
    location /ws/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade           $http_upgrade;
        proxy_set_header   Connection        "upgrade";
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
    }

    # 静态资源缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 方式二：Docker 部署

```dockerfile
# build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# production stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

```bash
docker build -t mufeng-frontend .
docker run -d -p 80:80 --name mufeng-frontend mufeng-frontend
```

### 方式三：Vite 预览服务器（仅小规模/内部使用）

```bash
npm run build
npm run preview -- --host 0.0.0.0 --port 4173
```

## 架构说明

- **状态管理**：基于 Vue 3 Composition API 的 `composables` 模式，使用 `provide/inject` 实现跨组件共享（参见 [composables/state.js](src/composables/state.js)）
- **API 层**：统一封装在 [api/](src/api/) 目录，使用 fetch API，集中管理请求头和错误处理
- **样式系统**：CSS 自定义属性（设计令牌）定义在 [styles/tokens.css](src/styles/tokens.css)，组件样式按模块拆分
- **实时通信**：WebSocket 用于订单状态变更的实时推送通知
