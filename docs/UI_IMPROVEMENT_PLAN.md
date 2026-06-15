# 项目 UI 改进规划进程书

> **参考项目**: [Art Design Pro](https://github.com/Daymychen/art-design-pro) (GitHub Stars 1.7k+)
> **参考源码**: `docs/art-design-pro-reference/`
> **创建日期**: 2026-06-11
> **适用范围**: `chat-frontend/` — 在线聊天系统前端

---

## 目录

1. [总纲](#1-总纲)
2. [现状分析](#2-现状分析)
3. [参考项目设计精华提取](#3-参考项目设计精华提取)
4. [改进阶段规划](#4-改进阶段规划)
5. [Phase 1: 设计语言统一](#phase-1-设计语言统一)
6. [Phase 2: 暗色模式](#phase-2-暗色模式)
7. [Phase 3: Admin 后台体验升级](#phase-3-admin-后台体验升级)
8. [Phase 4: 全站体验打磨](#phase-4-全站体验打磨)
9. [文件影响图谱](#5-文件影响图谱)
10. [风险与约束](#6-风险与约束)
11. [里程碑与验收标准](#7-里程碑与验收标准)

---

## 1. 总纲

### 1.1 核心原则

| 原则 | 说明 |
|------|------|
| **逻辑不动** | 所有改进仅限于样式、模板、CSS 变量层；`stores/`、`api/`、`composables/`（除 theme 相关）、`utils/websocket` 等业务逻辑文件不做任何修改 |
| **渐进式** | 分 4 个 Phase 递进，每个 Phase 独立可交付、可回滚 |
| **设计语言统一** | 以 Art Design Pro 的设计规范为参照，统一全站配色、圆角、阴影、动效语言 |
| **聊天为主，后台为辅** | 主聊天界面保持现有布局结构，仅升级视觉语言；Admin 区域可做更大幅度的设计对齐 |

### 1.2 技术栈对照

| 维度 | 当前项目 | Art Design Pro | 兼容性 |
|------|----------|----------------|:---:|
| 框架 | Vue 3 + Composition API | Vue 3 + Composition API | ✅ |
| 语言 | TypeScript 6.x | TypeScript | ✅ |
| 构建 | Vite 8 | Vite | ✅ |
| UI 库 | Element Plus 2.8 | Element Plus | ✅ |
| 图表 | Echarts 6 | Echarts | ✅ |
| 状态管理 | Pinia 3 | Pinia | ✅ |
| CSS 方案 | 自定义 CSS Variables | Tailwind CSS + SCSS | ⚠️ |
| 图标 | @element-plus/icons-vue | 相同 | ✅ |

---

## 2. 现状分析

### 2.1 当前项目 UI 架构

```
chat-frontend/src/
├── assets/styles/
│   ├── variables.css          ← 设计令牌（颜色、圆角、阴影、间距）
│   ├── reset.css              ← CSS Reset
│   └── main.css               ← Element Plus 全局样式覆盖（30+ 组件，~400行）
├── views/
│   ├── layout/                ← 主聊天布局（侧边栏 + Header + 内容区）
│   │   ├── MainLayout.vue     ← 可拖拽分割线布局
│   │   ├── Sidebar.vue        ← 好友/群聊/申请/印象 多 Tab 侧边栏
│   │   ├── Header.vue         ← 顶部栏（Logo + 通知铃铛 + 用户菜单）
│   │   └── Content.vue        ← 路由内容区
│   ├── auth/
│   │   ├── LoginView.vue      ← 登录页（毛玻璃卡片 + 动画背景）
│   │   └── RegisterView.vue   ← 注册页
│   ├── chat/
│   │   └── ChatView.vue       ← 聊天主视图
│   ├── admin/
│   │   ├── AdminView.vue      ← 管理后台（固定侧边栏布局）
│   │   └── components/        ← 7 个 Admin 子组件
│   └── profile/
│       └── ProfileView.vue    ← 个人资料页
├── components/
│   ├── chat/                  ← 聊天组件（消息气泡、输入框等）
│   ├── common/                ← 通用组件（对话框、加载、空状态等）
│   ├── call/                  ← 语音/视频通话组件
│   ├── rtc/                   ← WebRTC 组件
│   ├── friend/                ← 好友相关组件
│   ├── group/                 ← 群聊相关组件
│   └── message/               ← 消息盒子组件
├── stores/                    ← Pinia 状态管理（6 个 Store）
├── router/                    ← 路由配置
├── api/                       ← 接口层
├── composables/               ← 组合式函数
└── utils/                     ← 工具函数（websocket 等）
```

### 2.2 当前设计语言特征

| 设计要素 | 当前值/风格 | 评估 |
|----------|------------|------|
| **主色调** | `#5b6abf` (紫蓝) | 偏沉稳，可增加活力 |
| **圆角系统** | 8px / 12px / 16px 三级 | 合理，可微调 |
| **阴影层级** | 3 级 (light/base/dark) | 可增加层级 |
| **背景色** | `#f5f6f8` (浅灰) | 标准 |
| **动效** | `0.2s cubic-bezier` | 统一，可丰富 |
| **暗色模式** | ❌ 无 | 需新增 |
| **响应式** | 桌面端为主，侧边栏可拖拽 | 需增强移动端 |
| **Tailwind** | ❌ 未使用 | 可选引入 |
| **登录页** | 毛玻璃卡片 + 动画背景 | 已有设计感 |
| **Admin** | 基础 Element Plus 风格 | 提升空间大 |

---

## 3. 参考项目设计精华提取

从 Art Design Pro 中提取可直接参考的设计元素（源文件路径相对于 `docs/art-design-pro-reference/src/`）：

### 3.1 CSS 变量与主题系统

| 参考文件 | 提取内容 |
|----------|----------|
| `assets/styles/core/el-light.scss` | Element Plus 浅色主题 Sass 变量覆盖（主色、成功/警告/危险色、按钮 hover 样式、弹窗圆角） |
| `assets/styles/core/el-dark.scss` | 暗色模式 CSS 变量覆盖（背景、文字、边框、组件） |
| `assets/styles/core/dark.scss` | 暗色主题附加样式（富文本编辑器、分隔线、代码块等） |
| `assets/styles/core/app.scss` | 全局应用布局样式 |
| `assets/styles/core/theme-change.scss` | 主题切换过渡动画 |
| `assets/styles/core/theme-animation.scss` | 主题动效 |
| `assets/styles/core/tailwind.css` | Tailwind 入口配置 |

### 3.2 主题切换逻辑

| 参考文件 | 提取内容 |
|----------|----------|
| `hooks/core/useTheme.ts` | 主题切换 Hook（亮色/暗色/自动，含过渡禁用/启用、主色 9 级变体自动生成） |
| `store/modules/setting.ts` | 设置 Store（主题类型、主题色、圆角大小持久化） |
| `config/modules/component.ts` | 组件配置常量 |

### 3.3 UI 组件设计模式

| 参考组件 | 可借鉴方向 |
|----------|-----------|
| `components/core/cards/art-stats-card.vue` | 统计卡片（渐变数字 + hover 上浮动效） |
| `components/core/cards/art-line-chart-card.vue` | 图表卡片（卡片内嵌图表的标准布局） |
| `components/core/cards/art-data-list-card.vue` | 数据列表卡片 |
| `components/core/layouts/art-breadcrumb.vue` | 面包屑导航 |
| `components/core/layouts/art-global-search.vue` | 全局搜索 |
| `components/core/layouts/art-settings-panel/` | 设置面板（主题色/圆角/菜单风格可视化配置） |
| `components/core/forms/art-search-bar.vue` | 搜索栏组件 |

---

## 4. 改进阶段规划

```
Phase 1 ──→ Phase 2 ──→ Phase 3 ──→ Phase 4
(基础)      (主题)      (Admin)     (打磨)

每阶段独立可交付、可回滚
```

---

## Phase 1: 设计语言统一

> **目标**: 在不改任何业务逻辑的前提下，统一全站视觉语言
> **工期**: 2-3 天
> **风险**: 🟢 低

### 任务 1.1 — 升级 CSS 变量系统

**文件**: `src/assets/styles/variables.css`

**改动内容**:
- [ ] 扩展色板：在现有 4 个主色基础上，增加 9 级明度变体（`--color-primary-light-1` ~ `light-9`）
- [ ] 增加语义化颜色：`--color-bg-elevated`、`--color-bg-overlay`、`--color-border-hover`
- [ ] 增加阴影层级：从 3 级扩展到 5 级（增加 `--box-shadow-xl`、`--box-shadow-2xl`）
- [ ] 增加间距系统：`--spacing-xs` (4px) ~ `--spacing-3xl` (48px)
- [ ] 增加排版变量：`--font-size-xs` ~ `--font-size-3xl`、`--line-height-*`
- [ ] 参考 Art Design Pro 的色彩饱和度微调主色

**不影响逻辑**: ✅ 仅 CSS 变量定义，所有引用处自动生效

### 任务 1.2 — 优化 Element Plus 全局覆盖样式

**文件**: `src/assets/styles/main.css`

**改动内容**:
- [ ] 按钮 (`.el-button`): 增加 hover 态阴影扩散效果、active 态缩放微调已存在，增强 focus 态光环
- [ ] 输入框 (`.el-input__wrapper`): 增强 focus 态边框光晕（box-shadow 扩散半径加大）
- [ ] 对话框 (`.el-dialog`): 增加打开/关闭过渡动画
- [ ] 下拉菜单 (`.el-dropdown-menu`): 优化间距和 hover 背景色
- [ ] 标签 (`.el-tag`): 增加 subtle 变体样式
- [ ] 表格 (`.el-table`): 增加行 hover 过渡、表头 sticky 样式
- [ ] 分页 (`.el-pagination`): 优化按钮圆角和间距
- [ ] 卡片 (`.el-card`): 增加 hover 阴影提升效果
- [ ] 抽屉 (`.el-drawer`): 优化打开动画

**不影响逻辑**: ✅ 仅 CSS 覆盖，不涉及组件逻辑

### 任务 1.3 — 统一组件级样式变量引用

**涉及文件**: 所有 `.vue` 组件中硬编码的颜色/尺寸值

**改动内容**:
- [ ] 排查所有组件 scoped style 中的硬编码颜色值（如 `#f0f1ff`、`#f5f6f8` 等）
- [ ] 统一替换为 CSS 变量引用
- [ ] 确保所有颜色值通过变量系统管理

**不影响逻辑**: ✅ 纯样式替换

### Phase 1 验收标准
- [ ] 所有颜色值通过 CSS 变量管理
- [ ] 阴影系统覆盖全站交互状态
- [ ] 动效过渡统一流畅
- [ ] `npm run build` 无报错
- [ ] 主聊天界面布局不变、功能正常

---

## Phase 2: 暗色模式

> **目标**: 新增暗色模式支持，用户可切换
> **工期**: 3-4 天
> **风险**: 🟡 中

### 任务 2.1 — 创建 Theme Store

**文件**: `src/stores/themeStore.ts` (新建)

**改动内容**:
- [ ] 定义主题类型枚举 (`light` / `dark` / `auto`)
- [ ] 实现主题状态管理（当前主题、主题模式、主题色）
- [ ] 持久化到 localStorage
- [ ] 提供 `setTheme()`、`toggleTheme()`、`setThemeColor()` 方法

**不影响逻辑**: ✅ 新增文件，不修改现有 Store

### 任务 2.2 — 创建 useTheme Composable

**文件**: `src/composables/useTheme.ts` (新建)

**改动内容**:
- [ ] 封装主题切换逻辑
- [ ] 实现切换时临时禁用过渡动画（防闪烁）
- [ ] 监听系统偏好 (`prefers-color-scheme`) 支持 auto 模式
- [ ] 自动生成主色 9 级明度变体
- [ ] 参考 `docs/art-design-pro-reference/src/hooks/core/useTheme.ts`

**不影响逻辑**: ✅ 新增文件

### 任务 2.3 — 定义暗色 CSS 变量

**文件**: `src/assets/styles/dark.css` (新建)

**改动内容**:
- [ ] 定义 `html.dark` 或 `[data-theme="dark"]` 下的变量覆盖
- [ ] 背景色系统: 深灰层级 (`#141414` → `#1f1f1f` → `#2a2a2a`)
- [ ] 文字色系统: 白透明层级 (`rgba(255,255,255,0.85)` → `0.65` → `0.45`)
- [ ] 边框色: 暗色适配
- [ ] 阴影: 暗色下阴影变亮
- [ ] 滚动条暗色样式
- [ ] 参考 `docs/art-design-pro-reference/src/assets/styles/core/el-dark.scss`

**不影响逻辑**: ✅ 纯 CSS 文件

### 任务 2.4 — 创建主题切换入口

**涉及文件**:
- `src/views/layout/Header.vue` (新增主题切换按钮)
- `src/components/common/ThemeToggle.vue` (新建)

**改动内容**:
- [ ] Header 用户菜单旁增加主题切换图标按钮（太阳/月亮）
- [ ] 点击循环切换 浅色 → 暗色 → 自动
- [ ] 切换时添加旋转动画

**影响逻辑**: ⚠️ 轻微 — Header.vue 增加少量 script 逻辑（调用 themeStore）

### 任务 2.5 — 暗色模式适配关键页面

**涉及文件**: 聊天界面、登录页、Admin 页面

**改动内容**:
- [ ] 消息气泡在暗色下的配色（发送方/接收方气泡颜色）
- [ ] 登录页毛玻璃卡片暗色适配
- [ ] Admin 统计卡片暗色适配
- [ ] 侧边栏暗色适配
- [ ] Emoji 面板暗色适配

**不影响逻辑**: ✅ 纯样式适配

### Phase 2 验收标准
- [ ] 三种主题模式（亮/暗/自动）正常切换
- [ ] 切换过程无闪烁、无样式丢失
- [ ] 主题偏好刷新后保持
- [ ] 所有页面在暗色下可读、可用
- [ ] `npm run build` 无报错

---

## Phase 3: Admin 后台体验升级

> **目标**: 管理后台 UI 向 Art Design Pro 看齐
> **工期**: 3-5 天
> **风险**: 🟡 中

### 任务 3.1 — 升级统计卡片

**文件**: `src/views/admin/components/StatsCards.vue`

**改动内容**:
- [ ] 增加卡片图标（圆形渐变图标 + 背景装饰）
- [ ] 数值增加 count-up 数字滚动动画
- [ ] 增加同比/环比趋势指示器（↑↓ 箭头 + 百分比）
- [ ] 增强 hover 效果（阴影抬升 + 微缩放）
- [ ] 参考 `art-stats-card.vue` 的设计模式

**不影响逻辑**: ✅ 仅组件模板和样式

### 任务 3.2 — 升级数据表格

**涉及文件**:
- `src/views/admin/components/UserManage.vue`
- `src/views/admin/components/MessageAudit.vue`
- `src/views/admin/components/NotificationManage.vue`

**改动内容**:
- [ ] 表头样式优化（更明显的背景色区分）
- [ ] 行 hover 过渡动画
- [ ] 搜索栏区域重新布局（搜索框 + 筛选条件 + 操作按钮）
- [ ] 增加工具栏概念（左侧操作区 + 右侧搜索区）
- [ ] 空状态插图优化
- [ ] 分页组件居中并增加圆角卡片包裹

**不影响逻辑**: ✅ 仅模板布局和样式

### 任务 3.3 — 升级 Admin 侧边栏

**文件**: `src/views/admin/components/AdminSidebar.vue`

**改动内容**:
- [ ] Logo 区域增加图标
- [ ] 菜单项增加 tooltip 收起模式
- [ ] 激活态增加左侧指示条
- [ ] 菜单项间距和圆角微调
- [ ] 增加折叠/展开功能（可选）

**不影响逻辑**: ✅ 仅组件模板和样式

### 任务 3.4 — 增加面包屑导航

**文件**: `src/views/admin/components/AdminBreadcrumb.vue` (新建)
**关联文件**: `src/views/admin/AdminView.vue`

**改动内容**:
- [ ] 创建面包屑组件
- [ ] 在 Admin 内容区顶部渲染
- [ ] 支持路由自动生成 + 手动配置

**不影响逻辑**: ✅ 新增组件

### 任务 3.5 — 增加页面过渡动画

**文件**: `src/views/admin/AdminView.vue`

**改动内容**:
- [ ] 菜单切换时内容区使用 `<transition>` 包裹
- [ ] fade + slide 组合过渡效果

**影响逻辑**: ⚠️ 轻微 — AdminView.vue 模板增加 `<transition>` 包裹

### Phase 3 验收标准
- [ ] Admin 后台视觉风格与 Art Design Pro 一致
- [ ] 统计卡片有数字动画和趋势指示
- [ ] 菜单切换有平滑过渡
- [ ] 表格样式统一优化
- [ ] 所有 Admin 功能正常（搜索、分页、状态切换、通知管理）

---

## Phase 4: 全站体验打磨

> **目标**: 全站细节打磨，提升整体品质感
> **工期**: 3-5 天
> **风险**: 🟡 中

### 任务 4.1 — 可选引入 Tailwind CSS

**文件**: 
- `tailwind.config.ts` (新建)
- `postcss.config.js` (新建，如需要)
- `src/assets/styles/tailwind.css` (新建)

**改动内容**:
- [ ] 安装 `tailwindcss` + `@tailwindcss/vite`（Vite 8 原生插件方案）
- [ ] 配置 `important` 策略避免与 Element Plus 冲突
- [ ] 将 CSS 变量桥接到 Tailwind theme 配置
- [ ] 渐进式在 Admin 页面使用 utility class
- [ ] 参考 `docs/art-design-pro-reference/src/assets/styles/core/tailwind.css`

**不影响逻辑**: ✅ 构建配置变化，不影响业务代码

### 任务 4.2 — 侧边栏交互优化

**文件**: `src/views/layout/Sidebar.vue`

**改动内容**:
- [ ] Tab 切换增加滑动指示器动画
- [ ] 好友列表项 hover 态优化
- [ ] 在线状态指示点增加脉冲动画
- [ ] 未读角标增加弹跳动画
- [ ] 搜索框 focus 态优化

**不影响逻辑**: ✅ 纯样式和微动效

### 任务 4.3 — 消息气泡样式升级

**文件**: `src/components/chat/MessageBubble.vue`

**改动内容**:
- [ ] 气泡圆角微调（发送方和接收方差异化）
- [ ] 增加轻微渐变背景
- [ ] 消息出现动画（从底部滑入）
- [ ] 长按/右键菜单样式优化

**不影响逻辑**: ✅ 纯样式

### 任务 4.4 — 通用组件样式统一

**文件**: `src/components/common/*.vue`

**改动内容**:
- [ ] `Loading.vue` — 增加骨架屏加载变体
- [ ] `Empty.vue` — 更新空状态插图风格
- [ ] `ConfirmDialog.vue` — 优化按钮布局和图标
- [ ] `BaseDialog.vue` — 优化标题栏和底部栏
- [ ] `InfiniteScroll.vue` — 增加加载指示器动画

**不影响逻辑**: ✅ 纯样式和模板微调

### 任务 4.5 — 移动端适配增强

**涉及文件**: 全局 CSS + MainLayout.vue + 关键页面

**改动内容**:
- [ ] 增加响应式断点变量
- [ ] 移动端侧边栏改为 overlay 模式（从左侧滑出）
- [ ] 移动端字体和间距缩放
- [ ] 通话界面移动端适配

**影响逻辑**: ⚠️ 轻度 — MainLayout.vue 增加响应式判断逻辑

### Phase 4 验收标准
- [ ] Tailwind CSS 正常构建
- [ ] 全站动效流畅（60fps）
- [ ] 移动端基本可用
- [ ] 消息气泡有新消息入场动画
- [ ] `npm run build` 无报错

---

## 5. 文件影响图谱

### 受影响文件一览（按 Phase 标注）

```
chat-frontend/src/
│
├── assets/styles/
│   ├── variables.css            [P1] 扩展设计令牌
│   ├── main.css                 [P1] 优化全局覆盖样式
│   ├── dark.css                 [P2] 新增暗色变量
│   └── tailwind.css             [P4] 新增 Tailwind 入口
│
├── stores/
│   └── themeStore.ts            [P2] 新增主题 Store
│
├── composables/
│   └── useTheme.ts              [P2] 新增主题 composable
│
├── views/
│   ├── layout/
│   │   ├── Header.vue           [P2] 增加主题切换按钮
│   │   ├── Sidebar.vue          [P4] 侧边栏交互优化
│   │   └── MainLayout.vue       [P4] 移动端适配
│   ├── admin/
│   │   ├── AdminView.vue        [P3] 面包屑 + 过渡动画
│   │   └── components/
│   │       ├── StatsCards.vue   [P3] 卡片升级
│   │       ├── AdminSidebar.vue [P3] 侧边栏升级
│   │       ├── UserManage.vue   [P3] 表格升级
│   │       ├── MessageAudit.vue [P3] 表格升级
│   │       ├── NotificationManage.vue [P3] 表格升级
│   │       ├── AdminHeader.vue  [P3] 样式微调
│   │       └── AdminBreadcrumb.vue [P3] 新增
│   └── auth/
│       ├── LoginView.vue        [P2] 暗色适配
│       └── RegisterView.vue     [P2] 暗色适配
│
├── components/
│   ├── chat/
│   │   └── MessageBubble.vue    [P4] 气泡样式升级
│   ├── common/
│   │   ├── ThemeToggle.vue      [P2] 新增
│   │   ├── Loading.vue          [P4] 骨架屏
│   │   ├── Empty.vue            [P4] 插图风格
│   │   └── ConfirmDialog.vue    [P4] 样式优化
│   └── ...                      (其余组件仅暗色适配)
│
├── main.ts                       [P4] 引入 Tailwind
└── App.vue                       [P2] 初始化主题
```

### 完全不受影响的文件

```
src/api/              ← 接口层，完全不碰
src/utils/            ← 工具层（websocket 等），完全不碰
src/stores/           ← 现有 5 个 Store（user/friend/message/rtc/app），完全不碰
src/composables/      ← 现有 composables，完全不碰
src/router/           ← 路由配置，完全不碰
src/types/            ← 类型定义，完全不碰
```

---

## 6. 风险与约束

### 风险矩阵

| 风险 | 概率 | 影响 | 应对 |
|------|:---:|:---:|------|
| CSS 变量重命名导致样式丢失 | 低 | 高 | Phase 1 采用新增而非重命名策略，保留旧变量向后兼容 |
| 暗色模式下消息气泡可读性差 | 中 | 中 | Phase 2 专门逐页面审核暗色适配 |
| Tailwind 与 Element Plus 样式冲突 | 中 | 中 | Phase 4 配置 `important` + `preflight: false` |
| 移动端适配影响桌面布局 | 低 | 中 | Phase 4 使用 `@media` 隔离，充分测试 |
| 构建失败 | 低 | 高 | 每个 Phase 结束执行 `npm run build` 验证 |

### 约束条件

1. **禁止删除/重命名现有 CSS 变量**：Phase 1 采用新增变量方式，旧变量保留
2. **禁止修改 `main.ts` 中业务初始化逻辑**：仅允许新增 `import` 语句
3. **禁止改变组件 props/events 接口**：只改模板和样式
4. **每个 Phase 独立分支开发**：`feat/ui-phase-1` ~ `feat/ui-phase-4`
5. **每个 Phase 完成后 code review + build 验证**

---

## 7. 里程碑与验收标准

### M1: Phase 1 完成（设计语言统一）

   - [ ] CSS 变量系统扩展完成，色板覆盖 9 级明度
   - [ ] Element Plus 全局覆盖样式全面优化
   - [ ] 组件硬编码颜色全部变量化
   - [ ] `npm run build` 成功
   - [ ] 主聊天界面功能全量回归通过

### M2: Phase 2 完成（暗色模式）

   - [ ] 三种主题模式正常切换
   - [ ] 无闪烁、无样式断裂
   - [ ] 主题偏好持久化
   - [ ] 所有页面暗色适配通过
   - [ ] `npm run build` 成功

### M3: Phase 3 完成（Admin 升级）

   - [ ] 统计卡片带数字动画
   - [ ] 表格统一样式优化
   - [ ] 面包屑导航可用
   - [ ] 页面切换有过渡动画
   - [ ] `npm run build` 成功

### M4: Phase 4 完成（全站打磨）

   - [ ] Tailwind CSS 可选择性使用
   - [ ] 消息气泡有新消息动画
   - [ ] 移动端基本可用
   - [ ] 全站动效流畅
   - [ ] `npm run build` 成功

---

## 附录 A: 参考资源

| 资源 | 路径/链接 |
|------|----------|
| Art Design Pro 源码 | `docs/art-design-pro-reference/` |
| Art Design Pro GitHub | https://github.com/Daymychen/art-design-pro |
| Art Design Pro 在线预览 | https://www.lingchen.kim/art-design-pro |
| 当前项目 UI 变量 | `chat-frontend/src/assets/styles/variables.css` |
| 当前项目全局样式 | `chat-frontend/src/assets/styles/main.css` |

## 附录 B: 配色参考（Art Design Pro）

待 Phase 1 实施时从参考项目提取具体色值。
