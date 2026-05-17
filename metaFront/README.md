# 沐枫餐饮 - 顾客端

顾客在线点餐前端应用，提供菜品浏览、下单、订单状态跟踪和 AI 客服功能。

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
│   ├── api/              # API 请求模块（menu、orders、cart、chat）
│   ├── components/
│   │   ├── collection/   # 菜单浏览组件（CategoryTabs、SearchBox、ProductGrid、ProductCard）
│   │   ├── detail/       # 菜品详情组件（ProductInfo）
│   │   └── layout/       # 布局组件（AppHeader、AppCartDrawer、AppChatWidget）
│   ├── composables/      # 组合式函数（useProducts、useCart、useChat）
│   ├── config/           # 常量配置
│   ├── data/             # 静态兜底数据
│   ├── router/           # 路由定义
│   ├── styles/           # 设计令牌（tokens.css）
│   ├── utils/            # 工具函数（cartSession）
│   ├── views/            # 页面视图（CollectionView、ProductDetailView、OrderStatusView、NotFoundView）
│   ├── App.vue           # 根组件
│   └── main.js           # 应用入口
├── index.html            # HTML 入口
├── vite.config.js        # Vite 配置（端口 8888，代理 /api -> localhost:8080）
└── package.json          # 依赖与脚本
```

## 页面路由

| 路径 | 视图 | 说明 |
|------|------|------|
| `/` | - | 重定向到 `/menu` |
| `/menu` | CollectionView | 菜品菜单浏览（分类筛选 + 搜索） |
| `/menu/:id` | ProductDetailView | 菜品详情 |
| `/orders/:orderNo` | OrderStatusView | 订单状态实时跟踪（15 秒轮询） |
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

默认访问 `http://localhost:8888`。

开发服务器内置 API 代理：`/api/*` 请求转发到 `http://localhost:8080`。

### 3. 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/`。

## 生产部署

### Nginx 静态部署（推荐）

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

    # API 反向代理
    location /api/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
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
docker build -t mufeng-metafront .
docker run -d -p 80:80 --name mufeng-metafront mufeng-metafront
```

## 架构说明

- **状态管理**：基于 Vue 3 Composition API 的 composables 模式，购物车持久化到 localStorage
- **API 层**：统一封装 fetch 请求，含 15 秒超时和中文化错误提示
- **样式系统**：单一 `tokens.css` 定义设计令牌，毛玻璃卡片风格 + 暖金色调（`#af7b38`）
- **路由模式**：`createWebHistory` 模式，生产部署需 Nginx `try_files` 回退
- **AI 客服**：优先 SSE 流式回复，失败时降级为非流式 `queryChat`
