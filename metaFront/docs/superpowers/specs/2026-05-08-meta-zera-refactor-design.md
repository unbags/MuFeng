# Meta Zera 重构设计

## 概述

将 ZERA MAISON 时尚品牌官网从单文件 Vue 3 应用重构为组件化架构，引入 Vue Router，在保持玻璃态视觉风格的基础上优化升级。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | Composition API |
| Vue Router | 4.x | SPA 路由 |
| Vite | 6.x | 构建工具 |

## 路由设计

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | HomeView | Hero 轮播 + 服务卡片 + 礼赠区域 |
| `/collection` | CollectionView | 分类筛选 + 搜索 + 商品网格 |
| `/collection/:id` | ProductDetailView | 商品详情、加入购物袋 |
| `/:pathMatch(.*)*` | NotFoundView | 404 页面 |

## 组件树

```
App.vue
├── AppHeader.vue          # 顶栏：品牌名 + 导航 + 购物袋按钮
├── AppChatWidget.vue      # 全局智能客服悬浮窗
├── AppCartDrawer.vue      # 购物袋侧边抽屉
├── AppFooter.vue          # 页脚（新增）
└── <RouterView>
    ├── HomeView.vue
    │   ├── HeroCarousel.vue      # 轮播
    │   ├── ServiceCards.vue      # 服务卡片
    │   └── GiftBanner.vue        # 礼赠区域
    ├── CollectionView.vue
    │   ├── CategoryTabs.vue      # 分类标签
    │   ├── SearchBox.vue         # 搜索框
    │   └── ProductGrid.vue       # 商品网格
    │       └── ProductCard.vue   # 商品卡片
    └── ProductDetailView.vue
        └── ProductInfo.vue       # 商品详情
```

## 数据流

```
composables/
├── useCart.js        # 购物袋状态（provide/inject 全局共享）
├── useChat.js        # 客服消息状态
├── useProducts.js    # 商品数据 + 筛选 + 搜索
└── useCarousel.js    # 轮播自动播放
```

## 样式系统

保持 CSS 自定义属性令牌体系，按组件拆分样式：
- `src/styles/tokens.css` — 设计令牌
- `src/styles/base.css` — 重置 + 全局样式
- 每个组件的 scoped style 负责自身样式
