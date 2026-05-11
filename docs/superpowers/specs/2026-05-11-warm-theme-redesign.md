# 沐枫餐饮前端 · 暖色调全面优化设计文档

日期: 2026-05-11
状态: 已确认

---

## 1. 目标

将前端项目从当前 Teal 冷色调全面转换为 Vanilla 暖色调，保留现有 Apple 风格毛玻璃质感，实现视觉上的温馨、柔和、奶白色体验。

## 2. 设计决策

| 维度 | 当前 | 目标 |
|------|------|------|
| 配色方向 | Teal 冷色调 | Vanilla 香草暖色调 |
| 主品牌色 | `#168F7F` Teal | `#D4A47C` Apricot 杏色 |
| 视觉风格 | Frosted Glass 毛玻璃 | Cream Glass 奶霜玻璃（保留 blur 效果） |
| 背景基底 | `#F5F5F7` 冷灰白 | `#FDFAF6` 暖奶白 |
| 文字主色 | `#1D1D1F` 纯黑 | `#4A3728` 暖棕 |
| 文字辅色 | `#86868B` 冷灰 | `#A09080` 暖灰 |
| 辅助强调 | `#F06F5F` Coral | `#E0886E` 暖珊瑚 |
| 阴影基调 | rgba(0,0,0,*) 纯黑 | rgba(180,140,110,*) 暖棕 |

## 3. Token 映射表 (:root)

全局 CSS 自定义属性定义在 `tokens.css`。以下为变更对照：

| Token | 当前值 | 新值 |
|-------|--------|------|
| `--app-bg` | `#f5f5f7` | `#FDFAF6` |
| `--surface` | `#ffffff` | `#FFFDFA` |
| `--surface-soft` | `#fafafa` | `#FDF9F3` |
| `--surface-tint` | `#f0f7f5` | `#FDF5EC` |
| `--teal` | `#168f7f` | `#D4A47C` |
| `--teal-soft` | `#e8f5f2` | `#FDF1E7` |
| `--coral` | `#f06f5f` | `#E0886E` |
| `--coral-soft` | `#fff0ee` | `#FDF3EF` |
| `--yellow` | `#f3b84f` | `#E8B96A` |
| `--text` | `#1d1d1f` | `#4A3728` |
| `--muted` | `#86868b` | `#A09080` |
| `--border` | `#e5e5e7` | `#EDE4D8` |
| `--border-dark` | `rgba(0,0,0,0.08)` | `rgba(180,140,110,0.15)` |

语义色（`--success`, `--danger`）保持不变。

## 4. 阴影 Token 调整

所有 rgba 阴影从纯黑基调改为暖棕基调：

| Token | 新值 |
|-------|------|
| `--shadow` | `0 1px 3px rgba(180,140,110,0.06), 0 8px 24px rgba(180,140,110,0.08)` |
| `--shadow-sm` | `0 1px 2px rgba(180,140,110,0.04), 0 4px 12px rgba(180,140,110,0.06)` |
| `--shadow-md` | `0 2px 4px rgba(180,140,110,0.04), 0 12px 28px rgba(180,140,110,0.08)` |
| `--shadow-lg` | `0 2px 8px rgba(180,140,110,0.06), 0 20px 40px rgba(180,140,110,0.10)` |
| `--shadow-button` | `0 4px 14px rgba(224,136,110,0.28)` |
| `--shadow-teal` | `0 4px 14px rgba(212,164,124,0.32)` |
| `--shadow-card-hover` | `0 4px 12px rgba(180,140,110,0.06), 0 16px 32px rgba(180,140,110,0.12)` |
| `--shadow-modal` | `0 2px 8px rgba(180,140,110,0.06), 0 32px 72px rgba(180,140,110,0.14)` |

## 5. 页面级 Token 调整

### customer.css — `.customer-page`（堂食）
- `--order-accent`: `#168f7f` → `#D4A47C`
- `--order-accent-strong`: `#0d6f63` → `#B8805A`
- `--order-accent-soft`: `#e8f5f2` → `#FDF1E7`
- `--order-accent-glow`: `rgba(22,143,127,0.22)` → `rgba(212,164,124,0.24)`
- `--order-accent-faint`: `rgba(22,143,127,0.1)` → `rgba(212,164,124,0.12)`

### customer.css — `.customer-page.order-takeout`（外卖）
- `--order-accent`: `#f06f5f` → `#E0886E`
- `--order-accent-strong`: `#d94b3d` → `#C87860`
- `--order-accent-soft`: `#fff0ee` → `#FDF3EF`
- `--order-accent-glow`: `rgba(240,111,95,0.24)` → `rgba(224,136,110,0.26)`
- `--order-accent-faint`: `rgba(240,111,95,0.12)` → `rgba(224,136,110,0.14)`

### dashboard.css — `.dashboard-page`
- `--dashboard-teal`: `#168f7f` → `#D4A47C`
- `--dashboard-coral`: `#f06f5f` → `#E0886E`

## 6. 硬编码色值替换

以下文件中存在硬编码 hex 色值需要替换为 token 引用或新色值：

### base.css
- `rgba(22, 143, 127, *)` → `rgba(212, 164, 124, *)`（teal rgba → apricot rgba）
- `rgba(240, 111, 95, *)` → `rgba(224, 136, 110, *)`（coral rgba → warm coral rgba）
- `rgba(80, 68, 76, 0.94)` toast 背景 → `rgba(90, 55, 40, 0.94)` 暖棕
- `rgba(237, 224, 219, 0.95)` 错误面板边框 → 保持（已是暖色）
- `rgba(195, 176, 171, 0.12)` 阴影 → 保持（已是暖色）
- `.eyebrow/.section-label` 中的 `rgba(22, 143, 127, 0.1)` → `rgba(212, 164, 124, 0.12)`
- `.nav-item.active` 背景 `var(--teal)` → 自动跟随 token 更新

### components.css
- 所有 `var(--teal)` / `var(--teal-soft)` / `var(--coral)` → 自动跟随 token 更新
- `rgba(22, 143, 127, *)` 直接引用 → 替换为 apricot 对应值
- 渐变色中的 teal 值（如 `#74d1c7`, `#54b9ad`）→ 替换为暖调等价色
- `#f06f5f` → `#E0886E`

### layout.css
- `#f0e1dc` / `rgba(239, 224, 219, *)` → 保持或微调（已是暖色系）

### index.html
- `<meta name="theme-color">`: `#168f7f` → `#D4A47C`

## 7. 不改动的范围

- CSS 类名、组件结构、模板布局 — 完全不变
- 路由、状态管理、业务逻辑 — 完全不变
- `reset.css`, `animations.css`, `responsive.css` — 不涉及颜色变动
- Typography、Radius、Spacing、Transition tokens — 保持不变
- 语义色 `--success` (#34C759), `--danger` (#FF3B30) — 保持不变

## 8. 风险与注意事项

- **渐变硬编码** `components.css` 中存在多处 teal 系渐变色（如 `#74d1c7`, `#54b9ad`），需逐个替换为暖调对应色
- **毛玻璃透明度** sidebar 的 `rgba(255,255,255,0.72)` 保持不变，但基底色变暖后视觉感知会略有不同
- **对比度** 暖棕文字 `#4A3728` 在暖白背景上的对比度需验证满足 WCAG AA

## 9. 验证标准

- `npm run build` 无错误
- 所有页面视觉一致性（无残留冷调 teal 色块）
- 堂食/外卖双模式切换正常
- meta theme-color 正确显示暖色
