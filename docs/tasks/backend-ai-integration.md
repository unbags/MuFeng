# 后端工程师 -- AI 接口联调任务书

**日期:** 2026-05-11  
**关联文档:** [AI 助手升级计划书](../ai-assistant-upgrade-plan.md)  
**对接端:** MetaFront (顾客点餐端, Vue 3, localhost:8888)

---

## 1. 前置条件确认

开始联调前逐项检查：

- [ ] DeepSeek API Key 已配置：环境变量 `DEEPSEEK_API_KEY`
- [ ] DashScope API Key 已配置：环境变量 `DASHSCOPE_API_KEY`
- [ ] Milvus Standalone 已启动：`docker compose up -d` (端口 19530)
- [ ] Redis 已启动：`docker compose up -d` (端口 6379)
- [ ] MySQL 已启动并有菜单数据
- [ ] 后端可正常启动：`mvn spring-boot:run` (端口 8080)

---

## 2. 配置文件确认

**`application-dev.yml`** 中确认以下开关处于适合开发联调的状态：

```yaml
# 方案 A：渐进式联调（从简单到完整，推荐）
app:
  ai:
    enabled: true         # 第一步：仅 RAG 模式，验证基础 LLM 调用
    rag:
      enabled: true
    tools:
      enabled: false      # 第二步：确保 RAG 正常后，再改为 true 验证工具调用

spring:
  ai:
    model:
      chat: deepseek      # 不能是 "none"
      embedding: dashscope
    vectorstore:
      type: milvus        # 不能省略；默认 none 会跳过 Milvus 自动配置
      milvus:
        initialize-schema: true   # 开发环境 true，让 Spring AI 自动建 Collection
```

**方案 B：直接用完整模式**
```yaml
app:
  ai:
    enabled: true
    rag:
      enabled: true
      top-k: 5
      similarity-threshold: 0.65
    tools:
      enabled: true
      audit-enabled: true
```

---

## 3. 接口联调测试

### 3.1 基础连通性

```bash
# 测试 1：规则模式（关闭 AI 时仍可工作）
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message":"今天有什么推荐"}'

# 期望响应
# {
#   "code": 200,
#   "message": "ok",
#   "data": {
#     "message": "今日推荐：...",
#     "source": "MENU",
#     "repliedAt": "2026-05-11T..."
#   }
# }
```

### 3.2 AI RAG 模式测试

```bash
# 测试 2：菜品推荐（RAG 检索）
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message":"有什么不辣的推荐"}'

# 测试 3：规则问答（RAG 检索）
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message":"打包费多少钱"}'

# 测试 4：订单状态（应走工具调用或 fallback）
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message":"帮我查一下订单","orderNo":"20240510001"}'
```

关键验证点：
- `source` 字段：`"AI"` / `"AI-RAG"` / `"RULE"` 取决于模式
- 响应延迟：RAG 模式预计 2-5 秒，规则模式 < 100ms
- 不出现 500 错误

### 3.3 流式接口测试

```bash
# 测试 5：SSE 流式响应
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"message":"介绍一下招牌菜"}' \
  --no-buffer

# 期望：逐字流式输出文本内容（非 JSON）
```

### 3.4 异常降级测试

```bash
# 测试 6：超长消息（验证 400）
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"$(python3 -c 'print("x"*501)') \"}"

# 期望：{ "code": 400, "message": "问题不能超过500个字符", "data": null }
```

```bash
# 测试 7：AI 不可用时的降级
# 条件：临时关闭 AI (app.ai.enabled=false) 或停用 DeepSeek API
# 期望：source 为 "MENU" / "GUIDE" / "ORDER"，code 仍为 200
```

---

## 4. CORS 确认

`application.yml` 中的 CORS 配置需要覆盖 MetaFront 的端口：

```yaml
app:
  cors:
    allowed-origins: >
      http://localhost:8888,
      http://localhost:7777,
      http://localhost:5173
```

MetaFront 开发服务器运行在 **8888** 端口。如果浏览器控制台出现 CORS 错误，检查此项配置。

---

## 5. 提供给前端的接口契约

### POST /api/chat/query

```
Method: POST
Content-Type: application/json
Auth: 无

Request:
{
  "message": "string, max 500 chars, optional",
  "orderNo": "string, max 32 chars, optional"
}

Response 200:
{
  "code": 200,
  "message": "ok",
  "data": {
    "message": "AI 回复文本",
    "source": "AI|AI-RAG|RULE|MENU|ORDER|GUIDE",
    "repliedAt": "2026-05-11T12:00:00"
  }
}

Response 400:
{
  "code": 400,
  "message": "问题不能超过500个字符",
  "data": null
}
```

### POST /api/chat/stream

```
Method: POST
Content-Type: application/json
Accept: text/event-stream
Auth: 无

Request: 同上 { message, orderNo }

Response 200:
Content-Type: text/event-stream
Body: SSE 文本流，每个 data 事件是一个文本片段

前端使用 EventSource 或 fetch + ReadableStream 消费
```

---

## 6. 日志关键检查点

联调时应关注以下日志：

| 日志关键词 | 含义 |
|-----------|------|
| `Knowledge initialization complete` | 启动时知识库初始化成功 |
| `Knowledge initialization failed` | 知识库初始化失败，RAG 不可用 |
| `AI chat failed, falling back` | LLM 调用异常，已降级到规则回复 |
| `AI stream failed, falling back` | 流式调用异常，已降级 |
| `AI tool audit` | 工具调用审计日志（需 tools.audit-enabled=true） |

---

## 7. 前端可感知的 source 字段含义

前端可根据 `source` 字段调整 UI 展示：

| source | UI 表现建议 |
|--------|------------|
| `AI` | 显示 "AI 助手" 标识，启用流式动画 |
| `AI-RAG` | 显示 "AI 助手" 标识 |
| `RULE` / `MENU` / `ORDER` / `GUIDE` | 降级模式，无需 AI 标识，回复是固定模版 |

---

## 8. 联调完成标准

- [ ] `/api/chat/query` 在 AI 开启时返回 `source: "AI"` 或 `"AI-RAG"`
- [ ] `/api/chat/stream` 可正常流式输出
- [ ] AI 关闭时 `/api/chat/query` 正确降级，返回 200
- [ ] 超长消息返回 400，不影响服务稳定性
- [ ] MetaFront 跨域请求无 CORS 错误
- [ ] 知识库启动初始化日志正常打印
- [ ] RAG 命中文档可在日志中追踪（`logging.level.com.example.ordering.ai: DEBUG`）
