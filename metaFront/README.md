# 沐枫餐饮 - 顾客点餐端

顾客扫码点餐前端应用，提供菜品浏览、下单和订单状态跟踪功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式前端框架（Composition API） |
| Vue Router | 4.6 | 官方路由管理器 |
| Vite | 6.3 | 前端构建工具 |

## 项目结构

```
metaFront/
├── src/
│   ├── api/              # API 请求模块（menu、orders、chat）
│   ├── components/
│   │   ├── collection/   # 菜单浏览组件（分类标签、商品卡片、商品网格、搜索框）
│   │   ├── detail/       # 菜品详情组件
│   │   └── layout/       # 布局组件（顶部导航、购物车抽屉、智能客服）
│   ├── composables/      # 组合式函数（购物车、商品列表、智能客服）
│   ├── config/           # 常量配置
│   ├── data/             # 静态数据
│   ├── router/           # 路由定义
│   ├── styles/           # 样式文件（CSS Token、布局、组件、响应式）
│   ├── views/            # 页面视图（菜单列表、菜品详情、订单状态、404）
│   ├── App.vue           # 根组件
│   └── main.js           # 应用入口
├── index.html            # HTML 入口
├── vite.config.js        # Vite 配置
└── package.json          # 依赖与脚本
```

## 页面路由

| 路径 | 视图 | 说明 |
|------|------|------|
| `/menu` | CollectionView | 菜品菜单浏览 |
| `/menu/:id` | ProductDetailView | 菜品详情 |
| `/orders/:orderNo` | OrderStatusView | 订单状态跟踪 |
| `/:pathMatch(.*)*` | NotFoundView | 404 页面 |

根路径 `/` 自动重定向到 `/menu`。

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

默认访问 `http://localhost:8888`。

开发服务器内置 API 代理：所有 `/api/*` 请求会被转发到 `http://localhost:8080`（后端服务）。无需额外配置 CORS。

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
    server_name  order.your-domain.com;

    root   /opt/ordering/metafront/dist;
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

    # 静态资源缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 方式二：Docker 部署

```dockerfile
# 构建阶段
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

```bash
docker build -t mufeng-metafront .
docker run -d -p 80:80 --name mufeng-metafront mufeng-metafront
```

### 方式三：Vite 预览服务器（仅小规模/内部使用）

```bash
npm run build
npm run preview -- --host 0.0.0.0 --port 4173
```

## 架构说明

- **状态管理**：基于 Vue 3 Composition API 的 `composables` 模式，购物车状态与商品数据通过组合式函数管理
- **API 层**：统一封装在 [api/](src/api/) 目录，使用 fetch API
- **样式系统**：CSS 自定义属性定义设计令牌，组件样式按模块拆分
- **路由模式**：使用 `createWebHistory` 模式，生产部署需 Nginx 配合 `try_files` 回退
