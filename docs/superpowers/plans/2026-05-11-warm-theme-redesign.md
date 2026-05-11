# 暖色调全面优化 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前端从 Teal 冷色调全面转换为 Vanilla 暖色调（Cream Glass 风格）

**Architecture:** 集中式 token 驱动 — 所有改动以 `tokens.css` 的 CSS 变量重定义为核心，其余文件仅替换硬编码 hex/rgba 值。CSS 类名、组件结构、布局完全不变。

**Tech Stack:** Vue 3 + Vite，纯手写 CSS（无 Tailwind/PostCSS）

---

### Task 1: tokens.css — 全局 CSS 变量重定义

**Files:**
- Modify: `frontend/src/styles/tokens.css:1-67`

- [ ] **Step 1: 替换 tokens.css 中 :root 下的 color 变量和 shadow 变量**

找到并替换以下内容（精确匹配）：

**Color 变量（行 2-15）：**
```css
  --app-bg: #f5f5f7;
  --surface: #ffffff;
  --surface-soft: #fafafa;
  --surface-tint: #f0f7f5;
```
替换为：
```css
  --app-bg: #FDFAF6;
  --surface: #FFFDFA;
  --surface-soft: #FDF9F3;
  --surface-tint: #FDF5EC;
```

```css
  --text: #1d1d1f;
  --muted: #86868b;
  --border: #e5e5e7;
  --border-dark: rgba(0, 0, 0, 0.08);
```
替换为：
```css
  --text: #4A3728;
  --muted: #A09080;
  --border: #EDE4D8;
  --border-dark: rgba(180, 140, 110, 0.15);
```

```css
  --teal: #168f7f;
  --teal-soft: #e8f5f2;
  --coral: #f06f5f;
  --coral-soft: #fff0ee;
  --yellow: #f3b84f;
```
替换为：
```css
  --teal: #D4A47C;
  --teal-soft: #FDF1E7;
  --coral: #E0886E;
  --coral-soft: #FDF3EF;
  --yellow: #E8B96A;
```

**Shadow 变量（行 18-56）：**
```css
  --shadow: 0 1px 3px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.06);
```
替换为：
```css
  --shadow: 0 1px 3px rgba(180,140,110,0.06), 0 8px 24px rgba(180,140,110,0.08);
```

```css
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.03), 0 4px 12px rgba(0,0,0,0.04);
  --shadow-md: 0 2px 4px rgba(0,0,0,0.02), 0 12px 28px rgba(0,0,0,0.06);
  --shadow-lg: 0 2px 8px rgba(0,0,0,0.04), 0 20px 40px rgba(0,0,0,0.08);
```
替换为：
```css
  --shadow-sm: 0 1px 2px rgba(180,140,110,0.04), 0 4px 12px rgba(180,140,110,0.06);
  --shadow-md: 0 2px 4px rgba(180,140,110,0.04), 0 12px 28px rgba(180,140,110,0.08);
  --shadow-lg: 0 2px 8px rgba(180,140,110,0.06), 0 20px 40px rgba(180,140,110,0.10);
```

```css
  --shadow-button: 0 4px 14px rgba(240,111,95,0.24);
  --shadow-teal: 0 4px 14px rgba(22,143,127,0.28);
```
替换为：
```css
  --shadow-button: 0 4px 14px rgba(224,136,110,0.28);
  --shadow-teal: 0 4px 14px rgba(212,164,124,0.32);
```

```css
  --shadow-card-hover: 0 4px 12px rgba(0,0,0,0.04), 0 16px 32px rgba(0,0,0,0.1);
  --shadow-modal: 0 2px 8px rgba(0,0,0,0.04), 0 32px 72px rgba(0,0,0,0.12);
```
替换为：
```css
  --shadow-card-hover: 0 4px 12px rgba(180,140,110,0.06), 0 16px 32px rgba(180,140,110,0.12);
  --shadow-modal: 0 2px 8px rgba(180,140,110,0.06), 0 32px 72px rgba(180,140,110,0.14);
```

- [ ] **Step 2: 验证 tokens.css 无残留冷色值**

Run: `Select-String -Path "frontend/src/styles/tokens.css" -Pattern "168f7f|0d6f63|e8f5f2|f06f5f|d94b3d|fff0ee|f0f7f5|f3b84f|1d1d1f|86868b|e5e5e7|rgba\(0,0,0" -CaseSensitive:$false`

Expected: 无匹配结果

- [ ] **Step 3: 提交**

```bash
git add frontend/src/styles/tokens.css
git commit -m "style: 全局 CSS token 从 Teal 冷色调切换为 Vanilla 暖色调"
```

---

### Task 2: customer.css — 页面级 token 覆盖替换

**Files:**
- Modify: `frontend/src/styles/customer.css:4-21,165`

- [ ] **Step 1: 替换堂食模式 color token（`.customer-page` 块）**

行 5-9，将：
```css
  --order-accent: #168f7f;
  --order-accent-strong: #0d6f63;
  --order-accent-soft: #e8f5f2;
  --order-accent-glow: rgba(22, 143, 127, 0.22);
  --order-accent-faint: rgba(22, 143, 127, 0.1);
```
替换为：
```css
  --order-accent: #D4A47C;
  --order-accent-strong: #B8805A;
  --order-accent-soft: #FDF1E7;
  --order-accent-glow: rgba(212, 164, 124, 0.24);
  --order-accent-faint: rgba(212, 164, 124, 0.12);
```

- [ ] **Step 2: 替换外卖模式 color token（`.customer-page.order-takeout` 块）**

行 16-20，将：
```css
  --order-accent: #f06f5f;
  --order-accent-strong: #d94b3d;
  --order-accent-soft: #fff0ee;
  --order-accent-glow: rgba(240, 111, 95, 0.24);
  --order-accent-faint: rgba(240, 111, 95, 0.12);
```
替换为：
```css
  --order-accent: #E0886E;
  --order-accent-strong: #C87860;
  --order-accent-soft: #FDF3EF;
  --order-accent-glow: rgba(224, 136, 110, 0.26);
  --order-accent-faint: rgba(224, 136, 110, 0.14);
```

- [ ] **Step 3: 替换 focus 状态中的硬编码 teal rgba（行 165）**

将：
```css
  box-shadow: 0 0 0 3px rgba(22, 143, 127, 0.12);
```
替换为：
```css
  box-shadow: 0 0 0 3px rgba(212, 164, 124, 0.18);
```

- [ ] **Step 4: 验证无残留**

Run: `Select-String -Path "frontend/src/styles/customer.css" -Pattern "168f7f|0d6f63|e8f5f2|f06f5f|d94b3d|fff0ee|rgba\(22, 143, 127|rgba\(240, 111, 95" -CaseSensitive:$false`

Expected: 无匹配结果

- [ ] **Step 5: 提交**

```bash
git add frontend/src/styles/customer.css
git commit -m "style: customer.css 页面 token 切换为暖色调"
```

---

### Task 3: dashboard.css — 仪表板 token 替换

**Files:**
- Modify: `frontend/src/styles/dashboard.css:5-6,129`

- [ ] **Step 1: 替换 dashboard token 定义**

行 5-6，将：
```css
  --dashboard-teal: #168f7f;
  --dashboard-coral: #f06f5f;
```
替换为：
```css
  --dashboard-teal: #D4A47C;
  --dashboard-coral: #E0886E;
```

- [ ] **Step 2: 替换 range-toggle 中的硬编码 teal shadow（行 129）**

将：
```css
  box-shadow: 0 2px 8px rgba(22, 143, 127, 0.16);
```
替换为：
```css
  box-shadow: 0 2px 8px rgba(212, 164, 124, 0.20);
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/styles/dashboard.css
git commit -m "style: dashboard.css token 切换为暖色调"
```

---

### Task 4: base.css — 侧边栏与导航样式替换

**Files:**
- Modify: `frontend/src/styles/base.css:119,171`

- [ ] **Step 1: 替换 eyebrow/section-label 背景色（行 119）**

将：
```css
  background: rgba(22, 143, 127, 0.1);
```
替换为：
```css
  background: rgba(212, 164, 124, 0.12);
```

- [ ] **Step 2: 替换 nav-item.active 阴影（行 171）**

将：
```css
  box-shadow: 0 4px 14px rgba(22, 143, 127, 0.28);
```
替换为：
```css
  box-shadow: 0 4px 14px rgba(212, 164, 124, 0.32);
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/styles/base.css
git commit -m "style: base.css 硬编码色值切换为暖色调"
```

---

### Task 5: reset.css — focus outline 替换

**Files:**
- Modify: `frontend/src/styles/reset.css:38`

- [ ] **Step 1: 替换 focus outline 颜色（行 38）**

将：
```css
  outline: 3px solid rgba(22, 143, 127, 0.25);
```
替换为：
```css
  outline: 3px solid rgba(212, 164, 124, 0.30);
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/styles/reset.css
git commit -m "style: reset.css focus outline 切换为暖色调"
```

---

### Task 6: components.css — 组件内硬编码色值替换（9处）

**Files:**
- Modify: `frontend/src/styles/components.css:111,277,586,683,1134-1135,1214,1407,1477`

- [ ] **Step 1: 替换 .primary-btn:hover 阴影（行 111）**

将：
```css
  box-shadow: 0 6px 20px rgba(22, 143, 127, 0.36);
```
替换为：
```css
  box-shadow: 0 6px 20px rgba(212, 164, 124, 0.40);
```

- [ ] **Step 2: 替换 .dish-tag 背景（行 277）**

将：
```css
  background: rgba(22, 143, 127, 0.1);
```
替换为：
```css
  background: rgba(212, 164, 124, 0.12);
```

- [ ] **Step 3: 替换 form focus shadow（行 586）**

将：
```css
  box-shadow: 0 0 0 3px rgba(22, 143, 127, 0.12);
```
替换为：
```css
  box-shadow: 0 0 0 3px rgba(212, 164, 124, 0.18);
```

- [ ] **Step 4: 替换 bar-track 渐变（行 683）**

将：
```css
  background: linear-gradient(90deg, var(--teal), #74d1c7);
```
替换为：
```css
  background: linear-gradient(90deg, var(--teal), #E8C4A0);
```

- [ ] **Step 5: 替换 success-icon 渐变和阴影（行 1134-1135）**

将：
```css
  background: linear-gradient(135deg, var(--teal), #54b9ad);
  box-shadow: 0 14px 26px rgba(27, 138, 127, 0.22);
```
替换为：
```css
  background: linear-gradient(135deg, var(--teal), #C89870);
  box-shadow: 0 14px 26px rgba(180, 120, 90, 0.22);
```

- [ ] **Step 6: 替换 success-total-card 边框（行 1214）**

将：
```css
  border: 1px solid rgba(27, 138, 127, 0.2);
```
替换为：
```css
  border: 1px solid rgba(180, 120, 90, 0.20);
```

- [ ] **Step 7: 替换 upload drop zone 边框（行 1407）**

将：
```css
  border: 1px dashed rgba(27, 138, 127, 0.45);
```
替换为：
```css
  border: 1px dashed rgba(180, 120, 90, 0.35);
```

- [ ] **Step 8: 替换 knowledge bar-track 渐变（行 1477）**

将：
```css
  background: linear-gradient(90deg, var(--teal), #74d1c7);
```
替换为：
```css
  background: linear-gradient(90deg, var(--teal), #E8C4A0);
```

- [ ] **Step 9: 提交**

```bash
git add frontend/src/styles/components.css
git commit -m "style: components.css 9处硬编码色值切换为暖色调"
```

---

### Task 7: admin.css — 管理后台硬编码色值替换（10处）

**Files:**
- Modify: `frontend/src/styles/admin.css:73,82,95,263,327,382,474-475,527,562`

- [ ] **Step 1: 逐行替换所有 teal/coral rgba**

| 行号 | 当前值 | 替换为 |
|------|--------|--------|
| 73 | `rgba(22, 143, 127, 0.15)` | `rgba(212, 164, 124, 0.18)` |
| 82 | `rgba(240, 111, 95, 0.18)` | `rgba(224, 136, 110, 0.20)` |
| 95 | `rgba(22, 143, 127, 0.3)` | `rgba(212, 164, 124, 0.36)` |
| 263 | `rgba(22, 143, 127, 0.12)` | `rgba(212, 164, 124, 0.15)` |
| 327 | `rgba(22, 143, 127, 0.24)` | `rgba(212, 164, 124, 0.28)` |
| 382 | `rgba(22, 143, 127, 0.25)` | `rgba(212, 164, 124, 0.28)` |
| 474 | `rgba(22, 143, 127, 0.18)` | `rgba(212, 164, 124, 0.20)` |
| 475 | `rgba(22, 143, 127, 0.03)` | `rgba(212, 164, 124, 0.04)` |
| 527 | `rgba(22, 143, 127, 0.08)` | `rgba(212, 164, 124, 0.10)` |
| 562 | `rgba(22, 143, 127, 0.08)` | `rgba(212, 164, 124, 0.10)` |

逐个 Edit 替换每个位置。

- [ ] **Step 2: 验证无残留**

Run: `Select-String -Path "frontend/src/styles/admin.css" -Pattern "rgba\(22, 143, 127|rgba\(240, 111, 95" -CaseSensitive:$false`

Expected: 无匹配结果

- [ ] **Step 3: 提交**

```bash
git add frontend/src/styles/admin.css
git commit -m "style: admin.css 10处硬编码色值切换为暖色调"
```

---

### Task 8: index.html — 浏览器主题色

**Files:**
- Modify: `frontend/index.html:11`

- [ ] **Step 1: 替换 meta theme-color**

将：
```html
    <meta name="theme-color" content="#168f7f" />
```
替换为：
```html
    <meta name="theme-color" content="#D4A47C" />
```

- [ ] **Step 2: 提交**

```bash
git add frontend/index.html
git commit -m "style: index.html meta theme-color 切换为暖色调"
```

---

### Task 9: 构建验证与全局检查

- [ ] **Step 1: 构建项目**

Run: `cd frontend; npm run build`

Expected: BUILD SUCCESS，无错误

- [ ] **Step 2: 全局搜索残留冷色值**

Run the following grep across the entire `frontend/src/styles/` directory:

```powershell
Select-String -Path "frontend/src/styles/*.css" -Pattern "#168f7f|#0d6f63|#e8f5f2|#f06f5f|#d94b3d|#fff0ee|#f0f7f5|rgba\(22,\s*143,\s*127|rgba\(240,\s*111,\s*95|rgba\(27,\s*138,\s*127|#74d1c7|#54b9ad" -CaseSensitive:$false
```

Expected: 无匹配结果（tokens.css 中的 `--teal`/`--coral` 等 token 名称本身不算，只搜色值 hex/rgba）

- [ ] **Step 3: 检查 index.html**

Run: `Select-String -Path "frontend/index.html" -Pattern "#168f7f" -CaseSensitive:$false`

Expected: 无匹配结果

- [ ] **Step 4: 最终提交（如有遗漏修复）**

```bash
git add -A
git commit -m "style: 完成暖色调切换最后的清理验证"
```
