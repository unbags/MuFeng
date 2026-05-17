# 沐枫餐饮 - 店员管理端

店员工作台前端应用，提供点餐操作、数据看板、商品管理和知识库上传功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式前端框架（Composition API） |
| Vue Router | 4.6 | 官方路由管理器 |
| Vite | 5.4 | 前端构建工具 |

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 请求模块（auth、menu、orders、dishes、categories、dashboard、knowledge）
│   ├── components/
│   │   ├── admin/         # 管理端组件（菜品卡片、表单弹窗、图片上传等）
│   │   ├── cart/          # 购物车组件（面板、结算弹窗、小票弹窗）
│   │   ├── dashboard/     # 仪表盘组件（图表、品类仪表、订单类型分布、最近订单）
│   │   ├── layout/        # 布局组件（侧边栏、Toast、Loading）
│   │   ├── menu/          # 菜单组件（菜品卡片、点餐类型切换）
│   │   └── ui/            # 通用 UI 组件（分页、数量控制、指标卡片、空状态、图片容错）
│   ├── composables/       # 组合式函数（state、useAppStore、useAuth、useCart、useMenu、useAdmin、useOrders、useDashboard、useToast、useKnowledge）
│   ├── config/            # 常量配置（分页、费用、提示时长、备注长度限制、HTTP 错误消息）
│   ├── router/            # 路由定义（含 beforeEach 认证守卫）
│   ├── styles/            # 样式文件（tokens、reset、base、layout、components、customer、admin、dashboard、animations、responsive）
│   ├── utils/             # 工具函数（format、normalize）
│   ├── views/             # 页面视图（Login、Register、Customer、Dashboard、Admin、Knowledge、NotFound）
│   ├── App.vue            # 根组件
│   └── main.js            # 应用入口
├── index.html             # HTML 入口
├── vite.config.js         # Vite 配置（端口 7777，代理 /api -> localhost:8080）
└── package.json           # 依赖与脚本
```

## 页面路由

| 路径 | 视图 | 说明 | 认证 |
|------|------|------|------|
| `/login` | LoginView | 管理员登录 | 否 |
| `/register` | RegisterView | 首次注册管理员 | 否 |
| `/workbench` | CustomerView | 点餐工作台 | 是 |
| `/dashboard` | DashboardView | 数据看板 | 是 |
| `/products` | AdminView | 菜品/分类管理 | 是 |
| `/knowledge` | KnowledgeView | 知识库文件上传 | 是 |
| `/:pathMatch(.*)*` | NotFoundView | 404 | 否 |

路由守卫：未登录访问需认证页面 → 跳转 `/login?redirect=...`；已登录访问登录页 → 跳转 `/dashboard`。

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

默认访问 `http://localhost:7777`。

开发服务器内置 API 代理：`/api/*` 请求转发到 `http://localhost:8080`。

### 3. 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/`。

## 生产部署

### Nginx 静态部署（推荐）

构建后将 `dist/` 部署到服务器：

```nginx
server {
    listen       80;
    server_name  admin.your-domain.com;

    root   /opt/ordering/frontend/dist;
    index  index.html;

    # Vue Router hash 模式回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    }

    # WebSocket 代理
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

### Docker 部署

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

```bash
docker build -t mufeng-frontend .
docker run -d -p 80:80 --name mufeng-frontend mufeng-frontend
```

## 架构说明

- **状态管理**：基于 Vue 3 Composition API 的 composables 模式，`useAppStore` 为中央编排器，各 composable 通过直接导入 ES module 共享响应式状态
- **API 层**：统一封装 fetch 请求，自动注入 JWT Bearer Token，401 时自动跳转登录页
- **样式系统**：10 个 CSS 文件按职责拆分，设计令牌统一定义在 `tokens.css`（暖色调 Apple 风格）
- **实时通信**：通过后端 WebSocket 推送订单状态与看板刷新，前端通过 API 轮询感知变更
