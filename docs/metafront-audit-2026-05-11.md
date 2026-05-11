# metaFront 业务逻辑审计报告

> 审查日期: 2026-05-11 | 审查范围: metaFront/ 全部源码 (22 个文件) | 问题总计: 28 项

---

## 严重问题 (Critical) — 7 项

### 1. 死代码数据文件

**文件:** [metaFront/src/data/products.js](metaFront/src/data/products.js) (135 行)

`dishes` 和 `categories` 数组定义后从未被任何文件导入，是僵尸代码。品类格式 (`['全部', '招牌推荐', ...]` 字符串数组) 与实际使用的格式 (`[{id: 'all', label: '全部餐品'}, ...]` 对象数组) 不兼容。

### 2. 常量定义但未使用

**文件:** [metaFront/src/config/constants.js](metaFront/src/config/constants.js):1

`NOTE_MAX_LENGTH = 60` 被导出但从未导入，CartDrawer 模板中硬编码了 `maxlength="60"`。

### 3. SSE 流取消导致竞态

**文件:** [metaFront/src/composables/useChat.js](metaFront/src/composables/useChat.js):24-27

旧 SSE 流的 `abort()` 被调用后，其 `.then()` 链可能在新流启动后仍然触发回调，存在竞态风险。

### 4. 价格为 0 的静默降级

**文件:** [metaFront/src/composables/useProducts.js](metaFront/src/composables/useProducts.js):17

`price: Number(dish.price) || 0` — 当 API 返回缺失 price 字段时，`NaN || 0` 求值为 0，菜品静默变为免费。

### 5. API 字段名不匹配: `type` vs `orderType`

**文件:** [metaFront/src/components/layout/AppCartDrawer.vue](metaFront/src/components/layout/AppCartDrawer.vue):71

提交订单使用 `orderType`，但收据视图读取的是 `lastOrder.type`。若后端回显字段名为 `orderType`，则类型标签始终错误或为空。

### 6. 商品详情加载失败的错误提示不准确

**文件:** [metaFront/src/components/detail/ProductInfo.vue](metaFront/src/components/detail/ProductInfo.vue):20-55

API 加载失败时 `isLoading=false` 且 `dish=undefined`，落入 "菜品未找到" 分支，但实际原因是网络错误。composable 中的 `errorMessage` 未被展示。

### 7. `delivery` 订单类型命名错误

**文件:** [metaFront/src/composables/useCart.js](metaFront/src/composables/useCart.js):10,96-101

UI 标示为"外带"，但内部值使用 `'delivery'`（外卖配送）。本系统不收集配送地址，应改为 `'takeout'` 或 `'pickup'`。

---

## 重要问题 (Significant) — 6 项

### 8. API_BASE 重复定义

**文件:** [metaFront/src/api/chat.js](metaFront/src/api/chat.js):3

`import.meta.env.VITE_API_BASE_URL || '/api'` 与 [api/index.js](metaFront/src/api/index.js):1 完全重复。

### 9. quickQuestions 定义了但从未渲染

**文件:** [metaFront/src/composables/useChat.js](metaFront/src/composables/useChat.js):17

6 条快捷问题被定义、返回、解构，但模板中从未使用。

### 10. 缺少 .env.production 文件

`VITE_API_BASE_URL` 未设置生产环境默认值，部署后 API 请求将失败。

### 11. 支付状态显示原始英文

**文件:** [metaFront/src/views/OrderStatusView.vue](metaFront/src/views/OrderStatusView.vue):64

`{{ order.paymentStatus || 'UNPAID' }}` — 显示 "UNPAID"/"PAID" 原始英文，中文应用不应展示。

### 12. API 请求无超时控制

**文件:** [metaFront/src/api/index.js](metaFront/src/api/index.js):11-20

`fetch()` 无 `AbortController` 超时，若后端挂起 UI 将永久显示"加载中"。

### 13. 订单状态无轮询/实时更新

**文件:** [metaFront/src/views/OrderStatusView.vue](metaFront/src/views/OrderStatusView.vue):35

状态仅在 `onMounted` 时获取一次，用户需手动刷新查看进度变化。

---

## 中等问题 (Moderate) — 6 项

### 14. selectedId 状态无实际效果

**文件:** [metaFront/src/views/CollectionView.vue](metaFront/src/views/CollectionView.vue):9,17-19

`selectedId` 设置后立即 navigate 离开，选中态在页面跳转前仅存在几帧，无可见效果。

### 15. ingredients 和 desc 从同一字段映射

**文件:** [metaFront/src/composables/useProducts.js](metaFront/src/composables/useProducts.js):14,21-22

两者都来自 `dish.description`，详情页"食材"和描述展示相同内容。

### 16. 自动滚动未检查用户滚动位置

**文件:** [metaFront/src/components/layout/AppChatWidget.vue](metaFront/src/components/layout/AppChatWidget.vue):21-34

流式输出时每次 chunk 都强制滚到底部，即使用户正在阅读历史消息。

### 17. 订单号匹配正则过于简单

**文件:** [metaFront/src/composables/useChat.js](metaFront/src/composables/useChat.js):35

`/ORD\d+/i` 可能匹配到非订单号的字符串。

### 18. addToCart 每次打开购物车抽屉

**文件:** [metaFront/src/composables/useCart.js](metaFront/src/composables/useCart.js):64

即使在已打开的抽屉内增删也会冗余设置 `isCartOpen.value = true`。

### 19. 整个购物车数组做 deep watch

**文件:** [metaFront/src/composables/useCart.js](metaFront/src/composables/useCart.js):164

每次变更都序列化整个 cart 到 localStorage，应使用防抖或细粒度 watch。

---

## 轻微问题 (Minor) — 9 项

### 20. CSS 变量 --accent-rose 未使用

**文件:** [metaFront/src/styles/tokens.css](metaFront/src/styles/tokens.css):16

### 21. calories 和 rating 提取但未展示

**文件:** [metaFront/src/composables/useProducts.js](metaFront/src/composables/useProducts.js):17-18

### 22. 图片无懒加载

**文件:** [metaFront/src/components/collection/ProductCard.vue](metaFront/src/components/collection/ProductCard.vue):20

### 23. CartDrawer 缺少焦点陷阱

**文件:** [metaFront/src/components/layout/AppCartDrawer.vue](metaFront/src/components/layout/AppCartDrawer.vue):47-178

无 `aria-modal`、无 focus trap、无 `inert`。

### 24. 订单提交防重复不完善

**文件:** [metaFront/src/composables/useCart.js](metaFront/src/composables/useCart.js):105-138

`isSubmitting` 设置位置偏晚（在多个校验之后）。

### 25. 无错误日志/监控

**文件:** [metaFront/src/api/index.js](metaFront/src/api/index.js):3-9

错误仅展示给用户，未 log 到控制台或任何监控服务。

### 26. 多处缺少 ARIA 属性

**涉及文件:** AppCartDrawer、AppChatWidget、ProductCard、CategoryTabs — 缺少 role/tabindex/aria-selected 等。

### 27. scrollBehavior 冗余

**文件:** [metaFront/src/router/index.js](metaFront/src/router/index.js):33-35

`return { top: 0 }` 与 tokens.css 中 `scroll-behavior: smooth` 效果等价但覆盖了 smooth 行为。

### 28. 不可达的 204 检查

**文件:** [metaFront/src/api/index.js](metaFront/src/api/index.js):33

`if (response.status === 204)` 在 `response.text()` 和 `JSON.parse()` 之后，204 的响应体为空，会先被更前的空文本检查捕获。

---

## 汇总

| 严重度 | 数量 | 主要类型 |
|--------|------|----------|
| 严重 | 7 | 死代码、静默降级、字段不匹配、竞态 |
| 重要 | 6 | 重复逻辑、缺配置、无超时、无轮询 |
| 中等 | 6 | 冗余状态、重复映射、UX 瑕疵 |
| 轻微 | 9 | 可访问性、死 CSS、未使用字段、懒加载 |
