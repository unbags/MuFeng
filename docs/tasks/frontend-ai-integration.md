# 前端工程师 -- AI 助手联调任务书

**日期:** 2026-05-11  
**对接端:** 后端 Spring Boot (端口 8080, AI Chat API)  
**当前基线:** MetaFront 已有 `AppChatWidget` + `useChat` + `api/chat.js`，但仅支持旧版 POST JSON 模式，不支持 SSE 流式响应

---

## 1. 当前状态与目标

### 现有能力
- [AppChatWidget.vue](src/components/layout/AppChatWidget.vue) — 固定定位聊天面板，玻璃拟态风格，消息列表 + 快捷问题 + 输入框
- [useChat.js](src/composables/useChat.js) — 聊天状态管理，`askSupport()` 调用 `queryChat()` 发送问题
- [api/chat.js](src/api/chat.js) — 单次 POST `/api/chat/query`，接收完整 JSON 回复

### 联调目标
将聊天从"一次性回复"升级为"流式逐字展示 + source 来源标识 + 工具执行提示"。

---

## 2. 后端接口契约

### 非流式（现有） `POST /api/chat/query`

```
Request:  { "message": "今天有什么推荐", "orderNo": null }
Response: { "code": 200, "message": "ok", "data": { "message": "...", "source": "AI", "repliedAt": "..." } }
```

### 流式（新增） `POST /api/chat/stream`

```
Request:  同上
Response: Content-Type: text/event-stream
         每个 data 事件是一个文本片段，前端拼接成完整回复
```

### source 字段含义（前端 UI 展示依据）

| source | 含义 | UI 标识 |
|--------|------|---------|
| `"AI"` | 完整 AI（RAG + Tools） | 显示 "AI 助手" 徽章 |
| `"AI-RAG"` | AI 仅检索模式 | 显示 "AI 助手" 徽章 |
| `"MENU"` | 后端降级，规则式推荐菜单 | 无 AI 标识 |
| `"ORDER"` | 后端降级，规则式查订单 | 无 AI 标识 |
| `"GUIDE"` | 后端降级，引导用户提供信息 | 无 AI 标识 |
| `"RULE"` | 后端降级，纯规则 | 无 AI 标识 |

---

## 3. 文件改动清单

### 3.1 `src/api/chat.js` — 新增流式请求函数

**参考文件:** [src/api/chat.js](src/api/chat.js) (当前 8 行)  
**API 参考:** [src/api/index.js](src/api/index.js) (`API_BASE` 常量)

新增 `streamChat(payload)` 函数：

```js
const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

// 原有函数保留不变
export function queryChat(payload) { ... }

// 新增：SSE 流式聊天，返回 ReadableStream 控制器
export function streamChat(payload, onChunk, onDone, onError) {
  const controller = new AbortController()

  fetch(`${API_BASE}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}))
        throw new Error(errorBody.message || `请求失败 (${response.status})`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        // SSE 格式可能是 "data: xxx\n\n"
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''  // 最后一行可能是不完整的，保留到下次
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            onChunk(line.slice(6))  // 去掉 "data: " 前缀
          }
        }
      }

      // 处理剩余 buffer
      if (buffer.startsWith('data: ')) {
        onChunk(buffer.slice(6))
      }

      onDone()
    })
    .catch((error) => {
      if (error.name !== 'AbortError') {
        onError(error)
      }
    })

  return controller  // 调用方可 abort
}
```

**关键设计决策：**
- 不使用 `EventSource`（它只支持 GET），改用 `fetch` + `ReadableStream`
- 返回 `AbortController` 供调用方取消请求
- `queryChat()` 保留不动，作为非流式降级方案

---

### 3.2 `src/composables/useChat.js` — 接入流式响应

**参考文件:** [src/composables/useChat.js](src/composables/useChat.js) (当前 55 行)

修改 `askSupport()` 方法，新增以下能力：

1. **优先使用流式**：调用 `streamChat()` 替代 `queryChat()`
2. **逐字追加**：在 `supportMessages` 最后一条 assistant 消息中逐步拼接待回复文本
3. **降级方案**：流式失败时回退到 `queryChat()`
4. **source 来源标识**：在 assistant 消息中附带 `source` 字段
5. **AbortController 管理**：新消息发送时 abort 前一个未完成的流

```js
import { ref } from 'vue'
import { queryChat, streamChat } from '../api/chat.js'

const isSupportOpen = ref(false)
const supportInput = ref('')
const isSending = ref(false)
const isStreaming = ref(false)     // 新增：是否正在流式输出
const streamAbort = ref(null)      // 新增：当前流的 AbortController
const supportMessages = ref([
  { role: 'assistant', text: '...', source: null },
])

const quickQuestions = ['今日招牌菜推荐', '查询我的订单', '了解配送范围', '预约包厢']

export function useChat() {
  async function askSupport(question) {
    const text = question || supportInput.value.trim()
    if (!text || isSending.value) return

    // 取消上一个未完成的流
    if (streamAbort.value) {
      streamAbort.value.abort()
      streamAbort.value = null
    }

    supportMessages.value.push({ role: 'user', text })
    supportInput.value = ''
    isSupportOpen.value = true
    isSending.value = true
    isStreaming.value = true

    const orderMatch = text.match(/ORD\d+/i)
    const payload = {
      message: text,
      orderNo: orderMatch ? orderMatch[0].toUpperCase() : undefined,
    }

    // 插入占位 assistant 消息，后续逐字填充
    const assistantIndex = supportMessages.value.push({
      role: 'assistant',
      text: '',
      source: null,
      isStreaming: true,
    }) - 1

    const onChunk = (chunk) => {
      supportMessages.value[assistantIndex].text += chunk
    }

    const onDone = () => {
      supportMessages.value[assistantIndex].isStreaming = false
      isSending.value = false
      isStreaming.value = false
      streamAbort.value = null
    }

    const onError = async (error) => {
      // 流式失败，回退到非流式
      try {
        const response = await queryChat(payload)
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: response.message || '我已经收到您的问题。',
          source: response.source || null,
          isStreaming: false,
        }
      } catch {
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: '智能客服暂时不可用，请稍后再试或联系店员。',
          source: null,
          isStreaming: false,
        }
      } finally {
        isSending.value = false
        isStreaming.value = false
        streamAbort.value = null
      }
    }

    streamAbort.value = streamChat(payload, onChunk, onDone, onError)
  }

  function toggleSupport() { isSupportOpen.value = !isSupportOpen.value }
  function closeSupport() { isSupportOpen.value = false }

  return {
    isSupportOpen, supportInput, supportMessages, quickQuestions,
    isSending, isStreaming, askSupport, toggleSupport, closeSupport
  }
}
```

**消息对象结构变更：**

```js
// 旧结构
{ role: 'assistant', text: '...' }

// 新结构（向后兼容，新增字段为可选）
{ role: 'assistant', text: '...', source: null, isStreaming: false }
```

---

### 3.3 `src/components/layout/AppChatWidget.vue` — UI 升级

**参考文件:** [src/components/layout/AppChatWidget.vue](src/components/layout/AppChatWidget.vue) (当前 189 行)

在 `<template>` 的消息循环区域增加三个视觉元素：

#### A. 流式逐字输出光标

```html
<article
  v-for="(message, index) in supportMessages"
  :key="`${message.role}-${index}`"
  :class="['support-message', message.role]"
>
  {{ message.text }}
  <!-- 流式输出中显示闪烁光标 -->
  <span v-if="message.isStreaming" class="stream-cursor" aria-hidden="true">|</span>
</article>
```

CSS 闪烁动画：
```css
.stream-cursor {
  animation: blink 0.8s step-end infinite;
  color: var(--accent);
  font-weight: 200;
}

@keyframes blink {
  50% { opacity: 0; }
}
```

#### B. Source 来源徽章

在每条 assistant 消息末尾，如果 `message.source === 'AI'` 或 `'AI-RAG'`，显示小徽章：

```html
<span v-if="message.source === 'AI' || message.source === 'AI-RAG'" class="source-badge">
  AI 助手
</span>
```

```css
.source-badge {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--accent);
  border: 1px solid var(--accent);
  opacity: 0.7;
}
```

#### C. 工具执行提示（中期迭代）

在 `isStreaming` 期间，消息区域底部显示一个指示器：

```html
<div v-if="isStreaming" class="tool-indicator" aria-live="polite">
  思考中<span class="dot-pulse">...</span>
</div>
```

```css
.tool-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  font-size: 12px;
  color: var(--muted);
}

.dot-pulse::after {
  content: '';
  animation: dots 1.2s steps(4, end) infinite;
}

@keyframes dots {
  0%   { content: ''; }
  25%  { content: '.'; }
  50%  { content: '..'; }
  75%  { content: '...'; }
}
```

#### D. 自动滚动

消息列表在流式输出时应自动滚动到底部：

```js
// 在 <script setup> 中添加
import { ref, computed, watch, nextTick } from 'vue'

const messagesContainer = ref(null)

watch(
  () => supportMessages.value.length,
  () => nextTick(() => {
    const el = messagesContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
)

// 同时在流式每次 onChunk 时触发滚动（通过深度 watch 或用 computed）
```

简化方案：在 `onChunk` 回调中不做滚动（避免频繁操作），只在消息新增时滚动到底。流式文本更新导致的容器高度变化可以用 CSS `scroll-behavior: smooth` 让浏览器自动处理。

---

### 3.4 Vite 配置确认

**参考文件:** [vite.config.js](vite.config.js)

确认 `/api` 代理正确指向后端：

```js
// vite.config.js
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8888,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

不需要改动（当前配置已正确）。

---

## 4. 改动文件汇总

| 文件 | 动作 | 改动量 |
|------|------|--------|
| `src/api/chat.js` | 新增 `streamChat()` 函数 | +45 行 |
| `src/composables/useChat.js` | 重写 `askSupport()`，新增 `isStreaming`/`streamAbort` 状态 | 修改约 40 行 |
| `src/components/layout/AppChatWidget.vue` | 新增流式光标、source 徽章、工具指示器、自动滚动 | +30 行模板 + 40 行 CSS |

**总计约 150 行改动**，零新增依赖。

---

## 5. 接口请求格式（与后端联调用）

```js
// 非流式
POST /api/chat/query
Content-Type: application/json
{ "message": "今天有什么推荐菜", "orderNo": null }
→ { "code": 200, "data": { "message": "...", "source": "AI", "repliedAt": "..." } }

// 流式  
POST /api/chat/stream
Content-Type: application/json
{ "message": "今天有什么推荐菜", "orderNo": null }
→ text/event-stream 流
```

---

## 6. 验收清单

- [ ] 输入"今天有什么推荐的菜"，前端逐字展示 AI 回复
- [ ] 流式输出期间显示闪烁光标 `|`
- [ ] 流式输出完成后光标消失，显示 "AI 助手" 来源徽章
- [ ] Answering 期间发送按钮显示"发送中"且 disabled
- [ ] 点击快捷问题（如"今日招牌菜推荐"）同样走流式
- [ ] 发送新消息时，前一个未完成的流被正确 abort
- [ ] 流式请求失败时自动降级到非流式 POST（页面无报错）
- [ ] 后端 AI 关闭时（source 非 AI），不显示 AI 徽章
- [ ] 网络断开时显示友好错误提示
- [ ] 消息列表在流式输出中自动滚动到底
- [ ] 移动端（<560px）布局正常，输入框不被键盘遮挡
- [ ] 聊天面板与购物车抽屉不重叠（现有 panelRight 逻辑保持）
- [ ] 零控制台报错
