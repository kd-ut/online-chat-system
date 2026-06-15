---
name: ui-improvement-plan
description: UI 改进规划进程书，基于 Art Design Pro 参考，分 4 个 Phase
metadata:
  type: project
---

## UI 改进规划

基于 Art Design Pro (Vue 3 + Element Plus + Tailwind) 的 UI 改进方案，分 4 个 Phase 渐进实施，**核心原则是不改业务逻辑**。

- **Phase 1** (2-3天): 设计语言统一 — 升级 CSS 变量系统、优化 Element Plus 全局覆盖样式、统一组件样式变量引用
- **Phase 2** (3-4天): 暗色模式 — 新增 themeStore + useTheme + dark.css，支持亮/暗/自动三模式
- **Phase 3** (3-5天): Admin 后台体验升级 — 统计卡片动画、表格样式、面包屑、页面过渡
- **Phase 4** (3-5天): 全站打磨 — Tailwind CSS 可选引入、消息气泡动画、移动端适配

详细规划文件: `docs/UI_IMPROVEMENT_PLAN.md`
参考项目源码: `docs/art-design-pro-reference/`

**约束**:
- 禁止修改 stores/api/composables/utils/router 业务逻辑
- 每个 Phase 独立分支开发
- 每个 Phase 完成后 `npm run build` 验证

**Why**: 用户要求基于 Art Design Pro 的 UI 设计风格改进当前项目，在不改项目逻辑的前提下统一设计语言。
**How to apply**: 按 Phase 顺序逐步实施，从 `docs/UI_IMPROVEMENT_PLAN.md` 获取详细任务清单。每个 Phase 从 CSS 变量和样式层切入，逐步提升到组件级别改进。
