---
name: project-tech-stack
description: 当前项目技术栈和架构概览
metadata:
  type: reference
---

## 项目结构

```
/opt/webchat/
├── chat-frontend/     ← Vue 3 + TS + Vite 8 + Element Plus 2.8 + Pinia 3 + Echarts 6
├── chat-backend/      ← 后端服务
├── webrtc-sfu/        ← WebRTC SFU
└── docs/
    ├── UI_IMPROVEMENT_PLAN.md   ← UI 改进规划书
    └── art-design-pro-reference/ ← Art Design Pro 参考源码
```

## 前端关键文件

- CSS 变量: `chat-frontend/src/assets/styles/variables.css`
- 全局样式覆盖: `chat-frontend/src/assets/styles/main.css` (Element Plus 30+ 组件覆盖)
- 主布局: `chat-frontend/src/views/layout/MainLayout.vue` (侧边栏 + 可拖拽分割 + 右侧面板)
- Admin 后台: `chat-frontend/src/views/admin/AdminView.vue` (固定侧边栏布局)
- 路由: `chat-frontend/src/router/routes.ts` (5 条路由)
- Store: userStore, friendStore, messageStore, rtcStore, appStore
- 实时通信: websocket + socket.io + mediasoup (WebRTC)

## 前端设计语言

- 主色: `#5b6abf` (紫蓝)
- 圆角: 8px / 12px / 16px 三级
- 阴影: 3 级 (light/base/dark)
- 背景: `#f5f6f8`
- 动效: `0.2s cubic-bezier(0.4, 0, 0.2, 1)`
- 暗色模式: 未实现
- Tailwind: 未使用

[[ui-improvement-plan]]
