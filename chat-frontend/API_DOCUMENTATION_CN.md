# WebChat - 完整 API 文档与项目架构

> **版本**: 1.0 | **更新日期**: 2026-06-03 | **技术栈**: Vue 3 + TypeScript + Element Plus + Pinia

---

## 目录

1. [项目概述](#1-项目概述)
2. [部署架构](#2-部署架构)
3. [前端架构](#3-前端架构)
4. [组件树](#4-组件树)
5. [路由设计](#5-路由设计)
6. [状态管理](#6-状态管理)
7. [WebSocket 协议](#7-websocket-协议)
8. [HTTP API 参考](#8-http-api-参考)
   - [8.1 用户模块](#81-用户模块)
   - [8.2 好友模块](#82-好友模块)
   - [8.3 消息模块](#83-消息模块)
   - [8.4 群组模块](#84-群组模块)
   - [8.5 表情模块](#85-表情模块)
   - [8.6 通知模块](#86-通知模块)
   - [8.7 印象模块](#87-印象模块)
   - [8.8 管理模块](#88-管理模块)
9. [TypeScript 类型定义](#9-typescript-类型定义)
10. [WebRTC / SFU 架构](#10-webrtc--sfu-架构)
11. [CSS 设计令牌](#11-css-设计令牌)
12. [工具模块](#12-工具模块)

---

## 1. 项目概述

WebChat 是一个参考微信交互模式设计的全栈实时聊天应用。支持以下功能：

- **私聊**（文字、图片、表情、语音消息）
- **群聊**（成员管理、禁言、群公告）
- **语音/视频通话**（基于 WebRTC，多方视频采用 SFU 架构）
- **消息撤回**（发送后 2 分钟内可撤回）
- **表情系统**（内置系统表情 + 用户上传自定义表情）
- **好友系统**（好友申请、分组、备注、在线状态）
- **印象系统**（用户间的公开评价/留言）
- **管理后台**（用户管理、消息审计、数据统计、系统通知）
- **聊天记录下载**

### 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Vue 3.5（组合式 API + `<script setup>`） |
| 语言 | TypeScript 6.0 |
| 构建工具 | Vite 8.0 |
| UI 组件库 | Element Plus 2.8 |
| 状态管理 | Pinia 3.0 |
| HTTP 客户端 | Axios 1.7 |
| 路由 | Vue Router 5.0 |
| WebSocket | 原生 WebSocket API |
| 实时音视频 | mediasoup-client 3.20 + Socket.IO 4.8（SFU 信令） |
| 图表 | ECharts 6.0（管理后台统计） |
| 日期处理 | Day.js 1.11 |
| 后端 | Spring Boot（Java） |
| 数据库 | MySQL 8.0 + Redis 7 |
| 容器化 | Docker Compose |
| 进程管理 | PM2 |
| 对象存储 | 阿里云 OSS |

---

## 2. 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                    PM2 进程管理器                         │
│                                                          │
│  ┌──────────────────┐  ┌──────────────────────────────┐ │
│  │  webchat-stack   │  │        webrtc-sfu            │ │
│  │  (Docker Compose)│  │  (Node.js, 端口 3000)        │ │
│  │                  │  │  mediasoup SFU 服务器         │ │
│  └──────┬───────────┘  └──────────────────────────────┘ │
└─────────┼────────────────────────────────────────────────┘
          │
          │ Docker Compose
          │
    ┌─────┴───────────────────────────────────┐
    │          Docker 网络                      │
    │                                          │
    │  ┌──────────┐  ┌──────────┐  ┌────────┐ │
    │  │  MySQL   │  │  Redis   │  │ 后端   │ │
    │  │  :3306   │  │  :6379   │  │ :8080  │ │
    │  └──────────┘  └──────────┘  └───┬────┘ │
    │                                   │      │
    │  ┌────────────────┐              │      │
    │  │    前端        │              │      │
    │  │  (Nginx :80)  │──────────────┘      │
    │  │  对外 :8081    │                     │
    │  └────────────────┘                     │
    └──────────────────────────────────────────┘
```

### Nginx 路由规则（前端容器内）

| 路径 | 目标 | 说明 |
|------|------|------|
| `/` | 静态文件（`/usr/share/nginx/html`） | SPA 回退至 `index.html` |
| `/api/` | `http://backend:8080/` | API 代理（剥离 `/api` 前缀） |
| `/uploads/` | `http://backend:8080` | 文件服务代理 |
| `/ws` | `http://backend:8080` | WebSocket 升级代理 |
| `/socket.io/` | `http://webrtc-sfu:3000` | SFU 信令代理 |

### 环境变量

| 变量 | 位置 | 用途 |
|------|------|------|
| `VITE_API_BASE_URL` | 仅 `.env.development` | 开发环境 API 基地址 |
| `VITE_WS_URL` | 仅 `.env.development` | 开发环境 WebSocket 地址；**生产环境不设置**（自动从 `location.host` 推导） |
| `VITE_RTC_SOCKET_URL` | 构建时 | SFU Socket.IO 地址 |
| `VITE_TURN_URL` | 构建时（可选） | WebRTC TURN 服务器 |
| `VITE_TURN_USERNAME` | 构建时（可选） | TURN 用户名 |
| `VITE_TURN_CREDENTIAL` | 构建时（可选） | TURN 密码 |

---

## 3. 前端架构

### 目录结构

```
src/
├── api/                     # HTTP API 层（按模块拆分）
│   ├── request.ts           # Axios 实例、拦截器
│   ├── admin/index.ts       # 管理后台 API
│   ├── emoji/index.ts       # 表情 API
│   ├── friend/index.ts      # 好友 API
│   ├── group/index.ts       # 群组 API
│   ├── impression/index.ts  # 印象 API
│   ├── message/index.ts     # 消息 API
│   ├── notification/index.ts# 通知 API
│   └── user/index.ts        # 用户/认证 API
├── assets/
│   ├── audio/               # MP3 音频文件（通知音、来电铃声）
│   ├── images/              # 静态图片（Logo、默认头像）
│   └── styles/
│       ├── variables.css    # CSS 自定义属性（设计令牌）
│       ├── reset.css        # CSS 重置 + 基础样式
│       └── main.css         # 全局样式 + Element Plus 覆写
├── components/
│   ├── call/                # 语音/视频通话 UI
│   ├── chat/                # 私聊组件
│   ├── common/              # 通用组件（对话框、加载等）
│   ├── communication/       # 表情选择器、工具栏、录音
│   ├── friend/              # 好友列表项、添加好友对话框
│   ├── group/               # 群聊组件
│   ├── impression/          # 印象留言板
│   ├── message/             # 聊天窗口、消息盒子
│   ├── messageBox/          # 通知列表
│   ├── messageBubble/       # 消息气泡变体
│   ├── rtc/                 # RTC 通话对话框
│   └── user/                # 迷你用户资料卡
├── composables/             # Vue 组合式函数（可复用逻辑）
│   ├── useAuth.ts           # 登录/注册/退出
│   ├── useCallSignal.ts     # WebRTC 通话信令
│   ├── useClickOutside.ts   # （占位）
│   ├── useMessage.ts        # ElMessage 封装
│   ├── usePagination.ts     # （占位）
│   ├── useResizable.ts      # 侧边栏拖拽调整大小
│   ├── useWebRTC.ts         # 点对点 WebRTC
│   └── useWebSocket.ts      # WebSocket 连接管理
├── router/
│   ├── index.ts             # 路由实例 + 导航守卫
│   └── routes.ts            # 路由定义
├── stores/                  # Pinia 状态仓库
│   ├── appStore.ts          # 应用全局状态（主题、侧边栏）
│   ├── friendStore.ts       # 好友列表 + 好友申请状态
│   ├── messageStore.ts      # 未读消息计数状态
│   ├── rtcStore.ts          # RTC/SFU 状态（mediasoup）
│   ├── userStore.ts         # 认证令牌 + 用户信息
│   └── index.ts             # 统一导出
├── types/                   # TypeScript 类型定义
│   ├── admin.d.ts
│   ├── api.d.ts
│   ├── friend.d.ts
│   ├── impression.d.ts
│   ├── message.d.ts
│   ├── user.d.ts
│   └── index.ts
├── utils/
│   ├── audio.ts             # 音频播放工具
│   ├── date.ts              # 日期格式化（dayjs）
│   ├── download.ts          # 文件下载辅助
│   ├── notify.ts            # 简单通知封装
│   ├── storage.ts           # 带前缀的 LocalStorage 封装
│   └── websocket.ts         # WebSocket 服务单例
├── views/
│   ├── admin/               # 管理后台页面
│   ├── auth/                # 登录 + 注册页面
│   ├── chat/                # 主聊天视图
│   ├── layout/              # 主布局 + 侧边栏
│   └── profile/             # 个人资料页面
├── App.vue                  # 根组件
└── main.ts                  # 入口文件
```

### HTTP 请求流程

```
组件 → API 函数 → Axios 实例（/api/request.ts）
    │                            │
    │  请求拦截器                 │  注入 Bearer token（来自 userStore）
    │                            │
    ▼                            ▼
  后端 ← Nginx 代理 ← Axios 发送到 /api/*（baseURL: '/api'）
    │
    │  响应拦截器
    │  ├─ code === 200 → 直接返回 data
    │  ├─ code === 1005 → token 过期 → 强制退出登录
    │  └─ 其他 → 显示 ElMessage 错误提示
    ▼
  组件接收类型化响应
```

### 认证机制

- 基于 JWT 的认证
- Token 存储在 `localStorage` 中，键名前缀为 `chat_`
- Axios 请求拦截器自动为所有请求添加 `Authorization: Bearer <token>` 头
- 路由守卫检查 `meta.requiresAuth`，未登录重定向至 `/login`
- Token 过期（code 1005）或 HTTP 401 自动触发退出登录

---

## 4. 组件树

```
App.vue
├─ RtcCallDialog（全局，始终挂载）
└─ <router-view>
    │
    ├─ LoginView（登录页）
    │   └─ AnimatedBackground
    │
    ├─ RegisterView（注册页）
    │
    ├─ ProfileView（个人资料页）
    │
    ├─ AdminView（管理后台）
    │   ├─ AdminSidebar
    │   ├─ AdminHeader
    │   ├─ StatsCards
    │   ├─ StatsChart
    │   ├─ UserManage
    │   ├─ MessageAudit
    │   └─ NotificationManage
    │
    └─ MainLayout（主布局）
        ├─ Sidebar（侧边栏）
        │   ├─ SidebarHeader
        │   ├─ SidebarTabs
        │   ├─ FriendList（好友列表）
        │   │   └─ FriendGroup
        │   │       └─ FriendItem
        │   ├─ GroupList（群聊列表）
        │   ├─ RequestList（好友申请列表）
        │   │   └─ FriendRequestItem
        │   └─ ImpressionBoard（印象留言板）
        │       └─ ImpressionList
        │           └─ ImpressionItem
        ├─ resize-handle（拖拽手柄）
        └─ right-panel（右侧面板）
            ├─ Header（顶栏）
            │   ├─ bell-badge（通知铃铛角标）
            │   ├─ user-dropdown（用户下拉菜单）
            │   └─ MessageBox（消息盒子）
            │       ├─ MessageList
            │       │   └─ MessageItem
            │       └─ NotificationDialog
            └─ Content
                └─ ChatView（聊天视图）
                    ├─ ChatWindow（私聊窗口）
                    │   ├─ ChatHeader
                    │   ├─ MessageList
                    │   │   └─ MessageBubble
                    │   │       └─ VoiceMessage
                    │   ├─ MessageInput（消息输入区）
                    │   │   ├─ CommunicationBar
                    │   │   │   ├─ Toolbar
                    │   │   │   ├─ RecordingTip
                    │   │   │   └─ EmojiPicker
                    │   │   │       └─ EmojiGrid
                    │   │   └─ (textarea + 按钮)
                    │   ├─ CallDialog
                    │   └─ DownloadDialog
                    └─ GroupChatWindow（群聊窗口）
                        ├─ GroupChatHeader
                        ├─ GroupMessageList
                        ├─ GroupMessageInput
                        ├─ GroupManagementDialog
                        ├─ GroupMembersDialog
                        ├─ GroupNoticeDialog
                        └─ InviteFriendDialog
```

---

## 5. 路由设计

| 路径 | 名称 | 组件 | 需认证 | 需管理员 | 标题 |
|------|------|------|--------|----------|------|
| `/login` | Login | LoginView | 否 | 否 | 登录 |
| `/register` | Register | RegisterView | 否 | 否 | 注册 |
| `/` | Main | MainLayout | 是 | 否 | 聊天 |
| `/profile` | Profile | ProfileView | 是 | 否 | 个人资料 |
| `/admin` | Admin | AdminView | 是 | 是 | 管理后台 |

**主路由查询参数：**

| 参数 | 类型 | 用途 |
|------|------|------|
| `friendId` | number | 打开与指定好友的私聊 |
| `groupId` | number | 打开指定群聊 |

**路由守卫：**
- `requiresAuth !== false` → 必须登录（重定向至 `/login`）
- `requiresAdmin === true` → 必须具有 `role === 'admin'`（重定向至 `/`）

---

## 6. 状态管理

### Pinia 仓库

#### `userStore`（用户）
| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | `string \| null` | JWT 令牌 |
| `userInfo` | `UserInfo \| null` | 当前用户信息 |

| 方法 | 签名 | 说明 |
|------|------|------|
| `setToken` | `(token: string) => void` | 保存令牌至状态和 localStorage |
| `setUserInfo` | `(info: UserInfo) => void` | 保存用户信息至状态和 localStorage |
| `logout` | `() => void` | 清除令牌、用户信息和 localStorage |
| `isLoggedIn` | `() => boolean` | 检查是否已登录 |
| `isAdmin` | `() => boolean` | 检查是否为管理员 |

#### `friendStore`（好友）
| 状态 | 类型 | 说明 |
|------|------|------|
| `friendList` | `FriendGroupVO[]` | 按分组排列的好友列表 |
| `friendRequests` | `FriendRequestVO[]` | 待处理的好友申请 |

| 方法 | 说明 |
|------|------|
| `loadFriendList()` | 从 API 获取好友列表 |
| `loadFriendRequests()` | 从 API 获取好友申请列表 |
| `getGroupNames()` | 获取所有分组名称 |
| `getFriendById(userId)` | 在所有分组中查找好友 |
| `clearUnreadForFriend(friendId)` | 将未读计数重置为 0 |
| `incrementUnreadForFriend(friendId)` | 将未读计数加 1 |
| `updateFriendOnlineStatus(userId, isOnline)` | 更新在线状态 |

#### `messageStore`（消息）
| 状态 | 类型 | 说明 |
|------|------|------|
| `unreadCount` | `UnreadCountVO \| null` | 未读消息聚合 |

| 方法 | 说明 |
|------|------|
| `loadUnreadCount()` | 从 API 获取未读计数 |
| `clearUnreadForFriend(friendId)` | 清除某好友的未读条目 |

#### `appStore`（应用）
| 状态 | 类型 | 说明 |
|------|------|------|
| `sidebarCollapsed` | `boolean` | 侧边栏折叠状态 |
| `theme` | `string` | 当前主题 |
| `globalLoading` | `boolean` | 全屏加载状态 |

#### `rtcStore`（实时通信）— 基于 Mediasoup 的视频通话
| 状态 | 说明 |
|------|------|
| `visible` | 通话对话框可见性 |
| `roomId` / `roomTitle` | 当前房间信息 |
| `members` | 房间成员列表 |
| `remoteMedias` / `remoteVideos` / `remoteAudios` | 远端媒体流 |
| `localStream` | 本地媒体流 |
| `isJoined` / `isPublishing` / `isMicMuted` / `isCameraOff` | 通话状态标志 |

---

## 7. WebSocket 协议

### 连接

WebSocket 连接到 HTTP 服务器的同一主机，路径为 `/ws`，JWT 令牌作为查询参数。

```
URL: ws://<host>/ws?token=<jwt_token>
     或 wss://<host>/ws?token=<jwt_token>（HTTPS 环境）
```

**自动重连**：延迟 3 秒，无限重试。  
**心跳**：每 30 秒发送 `{"type": "ping"}`。

### 客户端 → 服务端消息

#### 发送私聊消息
```json
{
  "type": "message",
  "toUserId": 123,
  "content": "你好！",
  "messageType": 1,
  "duration": 5
}
```
- `messageType`：`1` = 文本，`2` = 图片，`3` = 表情，`4` = 语音
- `duration`：仅语音消息（类型 4）需要

#### 发送群聊消息
```json
{
  "type": "group_message",
  "groupId": 456,
  "content": "大家好！",
  "messageType": 1
}
```

#### 发送通话信令
```json
{
  "type": "call",
  "action": "offer|answer|ice-candidate|hangup",
  "toUserId": 123,
  "callType": "voice|video",
  "sdp": "...",
  "candidate": "...",
  "sdpMid": "...",
  "sdpMLineIndex": 0
}
```

#### 心跳
```json
{"type": "ping"}
```

### 服务端 → 客户端消息

每条消息都有一个 `type` 字段，用于路由到相应的回调：

| `type` 值 | 说明 | 路由目标 |
|-----------|------|----------|
| `message` | 私聊消息 + 印象 | `messageCallbacks` |
| `status` | 在线/离线状态更新 | `statusCallbacks` |
| `group_message` | 群聊消息 | `groupMessageCallbacks` |
| `call` | 通话信令（WebRTC/SFU） | `callSignalCallbacks` |
| `notification` | 管理员发送的系统通知 | `notificationCallbacks` |
| `friend_request` | 收到新的好友申请 | `friendRequestCallbacks` |
| `friend_request_handled` | 好友申请被同意/拒绝 | `friendRequestHandledCallbacks` |

### 回调注册（带清理机制）

每个 `on*` 方法都返回一个**取消订阅函数**，用于防止内存泄漏：

```typescript
const unsubscribe = websocketService.onMessage((data) => { ... })
// 稍后：
unsubscribe()  // 移除此回调
```

---

## 8. HTTP API 参考

### 基础配置

- **基础 URL**：`/api`（由 Nginx 代理至后端）
- **Content-Type**：`application/json`（文件上传除外：`multipart/form-data`）
- **认证**：请求头 `Authorization: Bearer <token>`
- **标准响应格式**：
  ```json
  { "code": 200, "message": "success", "data": ... }
  ```
- **错误码**：`200` = 成功，`1005` = token 过期，其他 = 业务错误

---

### 8.1 用户模块

#### `POST /user/login`
使用用户名和密码登录。

**请求体：**
```json
{ "username": "string", "password": "string" }
```

**响应：** `LoginResponse`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "john",
    "nickname": "张三",
    "avatar": "https://...",
    "signature": "你好，世界",
    "role": "user"
  }
}
```

#### `POST /user/register`
注册新账号。

**请求体：**
```json
{ "username": "string", "password": "string", "nickname": "string" }
```

#### `GET /user/me`
获取当前登录用户的个人信息。

**响应：** `UserInfo`

#### `PUT /user/profile`
更新当前用户的个人资料。

**请求体：**
```json
{ "nickname": "string?", "signature": "string?" }
```

**响应：** `UserInfo`

#### `POST /user/avatar`
上传头像图片。

**请求：** `multipart/form-data`，字段 `file`（图片文件）

**响应：** `string`（头像 URL）

#### `GET /user/{userId}`
获取其他用户的公开资料。

**响应：** `UserInfo`

---

### 8.2 好友模块

#### `GET /friend/search?keyword=xxx`
按关键词搜索用户（用户名/昵称）。

**响应：** `SearchUserVO[]`
```json
[{
  "userId": 2, "nickname": "李四", "avatar": null,
  "signature": "Hello", "remark": null, "isOnline": true
}]
```

#### `POST /friend/request`
发送好友申请。

**请求体：**
```json
{ "toUserId": 2, "message": "交个朋友吧！" }
```

#### `GET /friend/requests`
获取所有待处理的好友申请（收到的申请）。

**响应：** `FriendRequestVO[]`

#### `PUT /friend/request/{requestId}`
同意或拒绝好友申请。

**请求体：**
```json
{ "status": 1 }
```
`status`：`1` = 同意，`2` = 拒绝

#### `GET /friend/list`
获取当前用户的好友列表，按分组排列。

**响应：** `FriendGroupVO[]`
```json
[{
  "groupName": "我的好友",
  "friends": [{
    "id": 1, "userId": 2, "nickname": "李四", "avatar": null,
    "signature": "Hello", "remark": null, "groupName": "我的好友",
    "isOnline": true, "unreadCount": 0
  }]
}]
```

#### `DELETE /friend/{friendId}`
删除好友关系。

#### `PUT /friend/{friendId}/group`
将好友移动到其他分组。

**请求体：**
```json
{ "groupName": "亲密好友" }
```

#### `PUT /friend/{friendId}/remark?remark=xxx`
更新好友备注/别名。

**查询参数：** `remark`（字符串）

---

### 8.3 消息模块

#### `GET /message/history/{friendId}?page=1&size=20`
获取与指定好友的聊天记录（分页）。

**响应：** `PageResult<MessageVO>`
```json
{
  "total": 100,
  "records": [{
    "id": 1, "fromUserId": 1, "fromUserNickname": "张三",
    "fromUserAvatar": null, "toUserId": 2, "toUserNickname": "李四",
    "messageType": 1, "content": "你好！",
    "isRead": true, "isRecalled": false, "sendTime": "2026-06-03T10:00:00"
  }]
}
```

**消息类型：**
| 值 | 类型 |
|----|------|
| 1 | 文本 |
| 2 | 图片 |
| 3 | 表情 |
| 4 | 语音 |

#### `GET /message/download/{friendId}?limit=100`
下载聊天记录为 `.txt` 文件。返回文件 blob。

**查询参数：** `limit`（最大消息条数，默认 100）

#### `PUT /message/read/{friendId}`
将某好友的所有消息标记为已读。

#### `GET /message/unread/count`
获取所有好友的未读消息计数。

**响应：** `UnreadCountVO`
```json
{
  "total": 5,
  "details": [{ "friendId": 2, "friendNickname": "李四", "friendAvatar": null, "unreadCount": 3 }],
  "messages": [{ "id": 10, "fromUserId": 2, "fromUserNickname": "李四", "fromUserAvatar": null, "content": "Hi", "sendTime": "...", "messageType": 1 }]
}
```

#### `PUT /message/recall/{messageId}`
撤回消息（需在 2 分钟内）。

#### `POST /message/upload/image`
上传图片文件用于聊天发送。

**请求：** `multipart/form-data`，字段 `file`（图片文件）

**响应：** `string`（图片 URL）

#### `POST /message/upload/voice`
上传语音录音文件。

**请求：** `multipart/form-data`，字段 `file`（音频文件）

**响应：** `string`（语音文件 URL）

---

### 8.4 群组模块

#### `POST /group`
创建新群聊。

**请求体：**
```json
{
  "name": "团队群聊",
  "avatar": "https://...",
  "notice": "欢迎加入！",
  "memberIds": [2, 3, 4]
}
```

**响应：** `GroupVO`

#### `GET /group/list`
获取当前用户所在的群聊列表。

**响应：** `GroupVO[]`
```json
[{
  "id": 1, "name": "团队群聊", "avatar": null,
  "notice": "欢迎！", "ownerId": 1, "memberCount": 5,
  "unreadCount": 2, "createdAt": "2026-06-01T00:00:00"
}]
```

#### `GET /group/{groupId}`
获取群组详情。

**响应：** `GroupVO`

#### `GET /group/message/{groupId}?page=1&size=20`
获取群聊历史记录（分页）。

**响应：** `{ total: number, records: GroupMessageVO[] }`

#### `GET /group/{groupId}/members`
获取群成员列表。

**响应：** `GroupMemberVO[]`
```json
[{
  "userId": 1, "nickname": "张三", "avatar": null,
  "groupNickname": null, "role": 2, "muted": false
}]
```
**角色说明：** `0` = 普通成员，`1` = 管理员，`2` = 群主

#### `POST /group/invite`
邀请用户加入群聊。

**请求体：**
```json
{ "groupId": 1, "userId": 5 }
```

#### `DELETE /group/{groupId}/quit`
退出群聊。

#### `DELETE /group/{groupId}/disband`
解散群聊（仅群主可操作）。

#### `PUT /group/{groupId}/read`
清除群聊未读计数。

#### `PUT /group/{groupId}/notice`
更新群公告。

**请求体：**
```json
{ "notice": "新的公告内容" }
```

#### `PUT /group/{groupId}/member/{memberId}/set-admin`
将成员提升为管理员。

#### `PUT /group/{groupId}/member/{memberId}/remove-admin`
取消管理员身份。

#### `DELETE /group/{groupId}/member/{memberId}`
从群聊中移除成员（群主/管理员可操作）。

#### `PUT /group/{groupId}/member/{memberId}/mute`
禁言某成员。

**请求体：**
```json
{ "minutes": 30 }
```

#### `PUT /group/{groupId}/member/{memberId}/unmute`
取消禁言。

#### `PUT /group/{groupId}/members/batch-mute`
批量禁言。

**请求体：**
```json
{ "memberIds": [2, 3], "minutes": 60 }
```

---

### 8.5 表情模块

#### `GET /emoji/system`
获取内置系统表情包。

**响应：** `EmojiVO[]`
```json
[{
  "id": 1, "name": "微笑", "url": "https://...",
  "category": "default", "isSystem": true, "createdAt": "..."
}]
```

#### `GET /emoji/user`
获取当前用户上传的自定义表情。

**响应：** `EmojiVO[]`

#### `POST /emoji/upload`
上传自定义表情。

**请求：** `multipart/form-data`
| 字段 | 类型 | 说明 |
|------|------|------|
| `file` | File | 图片文件 |
| `name` | String | 表情名称（1-20 字符，字母、数字或中文） |
| `category` | String? | 分类（可选） |

**响应：** `EmojiVO`

#### `DELETE /emoji/{emojiId}`
删除自定义表情。

---

### 8.6 通知模块

#### `GET /system-notification/unread`
获取未读系统通知（静默请求，不弹出错误提示）。

**响应：**
```json
{
  "total": 2,
  "notifications": [{ "id": 1, "title": "...", "content": "...", "adminId": 1, "adminNickname": "管理员", "createdAt": "..." }]
}
```

#### `PUT /system-notification/read/{notificationId}`
将通知标记为已读。

#### `POST /system-notification/send`
管理员：发送全站系统通知。

**请求体：**
```json
{ "title": "公告", "content": "服务器将于凌晨 3:00 重启" }
```

#### `GET /admin/notifications`
管理员：获取已发送的通知列表（静默请求）。

---

### 8.7 印象模块

"印象"是用户之间可以互相留下的公开评价/留言。

#### `POST /impression`
给其他用户添加印象。

**请求体：**
```json
{ "toUserId": 2, "content": "非常友好且乐于助人！" }
```

#### `GET /impression/to-me`
获取别人给我的印象。

**响应：** `ImpressionVO[]`

#### `GET /impression/by-me`
获取我给别人的印象。

**响应：** `ImpressionVO[]`

#### `DELETE /impression/{impressionId}`
删除印象。

---

### 8.8 管理模块

需要管理员权限（`user.role === 'admin'`）。

#### `GET /admin/stats`
获取仪表盘统计数据。

**响应：** `StatisticsVO`
```json
{ "totalUsers": 150, "todayActiveUsers": 42, "todayMessages": 320, "onlineUsers": 18 }
```

#### `GET /admin/users?page=1&size=10&keyword=xxx`
获取用户列表，支持分页和可选搜索。

**响应：** `PageResult<UserManageVO>`

#### `PUT /admin/user/{userId}/status?status=0`
启用或禁用用户账号。
`status`：`0` = 禁用，`1` = 启用

#### `GET /admin/messages?params...`
获取消息审计记录（分页，可筛选）。

**响应：** `PageResult<MessageAuditVO>`

---

## 9. TypeScript 类型定义

### 核心消息类型

```typescript
// 消息类型枚举（数值型）
// 1 = 文本, 2 = 图片, 3 = 表情, 4 = 语音

interface MessageVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar?: string | null
  toUserId: number
  toUserNickname: string
  messageType: number   // 1|2|3|4
  content: string
  isRead: boolean
  isRecalled: boolean
  sendTime: string       // ISO 日期字符串
  duration?: number      // 语音消息时长（秒）
}
```

### 用户类型

```typescript
interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  signature: string | null
  role: 'user' | 'admin'
}

interface LoginResponse {
  token: string
  user: UserInfo
}
```

### 好友类型

```typescript
interface FriendVO {
  id: number; userId: number; nickname: string
  avatar: string | null; signature: string | null
  remark: string | null; groupName: string
  isOnline: boolean; unreadCount: number
}

interface FriendGroupVO {
  groupName: string
  friends: FriendVO[]
}

interface FriendRequestVO {
  id: number; fromUserId: number
  fromUserNickname: string; fromUserAvatar: string | null
  message: string | null
  status: number  // 0=待处理, 1=已同意, 2=已拒绝
  createdAt: string
}
```

### 群组类型

```typescript
interface GroupVO {
  id: number; name: string; avatar: string | null
  notice: string | null; ownerId: number
  memberCount: number; unreadCount: number; createdAt: string
}

interface GroupMemberVO {
  userId: number; nickname: string; avatar: string | null
  groupNickname: string | null
  role: number   // 0=成员, 1=管理员, 2=群主
  muted?: boolean
}

interface GroupMessageVO {
  id: number; groupId: number; fromUserId: number
  fromUserNickname: string; fromUserAvatar: string | null
  content: string; messageType: number; sendTime: string
}
```

### 通用类型

```typescript
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

interface PageResult<T = any> {
  total: number
  records: T[]
}
```

---

## 10. WebRTC / SFU 架构

### 概述

项目有两套通话机制：

| 功能 | 技术方案 | 信令通道 |
|------|---------|---------|
| **语音通话（P2P）** | WebRTC 点对点（STUN/TURN） | WebSocket（`type: "call"`） |
| **视频通话（SFU）** | mediasoup SFU，通过 `webrtc-sfu` 服务 | Socket.IO（端口 3000） |

### SFU 视频通话流程

```
客户端 A ←── Socket.IO ──→ webrtc-sfu（Node.js + mediasoup）←── Socket.IO ──→ 客户端 B
              │                                                               │
              └──────────────── 媒体流（RTP）通过 mediasoup ──────────────────┘
```

**RTC Socket.IO 信令命令：**

| 命令 | 方向 | 用途 |
|------|------|------|
| `createRoomInvite` | 客户端 → 服务器 | 创建房间并邀请用户 |
| `respondRoomInvite` | 客户端 → 服务器 | 接受/拒绝邀请 |
| `joinRoom` | 客户端 → 服务器 | 加入房间，获取 RTP 能力 |
| `createWebRtcTransport` | 客户端 → 服务器 | 创建 mediasoup 传输通道 |
| `connectTransport` | 客户端 → 服务器 | 连接 DTLS 传输 |
| `produce` | 客户端 → 服务器 | 开始发送媒体 |
| `consume` | 客户端 → 服务器 | 开始接收媒体 |
| `resume` | 客户端 → 服务器 | 恢复消费者 |
| `pauseProducer` | 客户端 → 服务器 | 暂停生产者 |
| `resumeProducer` | 客户端 → 服务器 | 恢复生产者 |
| `leaveRoom` | 客户端 → 服务器 | 离开房间 |
| `endRoom` | 客户端 → 服务器 | 结束房间（创建者） |

**服务器 → 客户端事件：**

| 事件 | 用途 |
|------|------|
| `incomingInvite` | 有人邀请您加入通话 |
| `inviteResponded` | 您的邀请被接受/拒绝 |
| `roomMembersUpdate` | 成员列表发生变化 |
| `newProducer` | 新的媒体生产者可用 |
| `producerClosed` | 媒体生产者关闭 |
| `peerLeft` | 对端离开房间 |
| `roomEnded` | 房间已结束 |
| `disconnect` | Socket 断开连接 |

### 关键配置

- **SFU 端口**：3000（Socket.IO 信令）
- **RTC 端口**：2000-2100（mediasoup 媒体流）
- **STUN**：`stun.l.google.com:19302`
- **TURN**：可选，通过 `VITE_TURN_URL`/`VITE_TURN_USERNAME`/`VITE_TURN_CREDENTIAL` 配置
- **重新加入宽限期**：30 秒（允许页面刷新而不丢失房间成员身份）

---

## 11. CSS 设计令牌

### 颜色调色板

| 令牌 | 值 | 用途 |
|------|------|------|
| `--color-primary` | `#5b6abf` | 主按钮、激活状态、链接 |
| `--color-primary-light` | `#8b9cf7` | 悬停背景、聚焦环 |
| `--color-primary-dark` | `#4554a0` | 按钮悬停、强调 |
| `--color-success` | `#20a39e` | 成功消息/徽章 |
| `--color-warning` | `#f0a050` | 警告 |
| `--color-danger` | `#e87461` | 错误、危险操作 |
| `--color-info` | `#8e99a4` | 信息提示徽章 |

### 文字颜色

| 令牌 | 值 | 用途 |
|------|------|------|
| `--text-primary` | `#1f2937` | 标题、正文 |
| `--text-regular` | `#6b7280` | 次要文字、标签 |
| `--text-secondary` | `#9ca3af` | 弱化/三级文字 |
| `--text-placeholder` | `#d1d5db` | 输入框占位符 |

### 背景颜色

| 令牌 | 值 | 用途 |
|------|------|------|
| `--bg-color` | `#f5f6f8` | 页面背景 |
| `--bg-color-white` | `#ffffff` | 卡片/面板背景 |
| `--bg-color-overlay` | `#ffffff` | 下拉菜单/模态框背景 |

### 边框颜色

| 令牌 | 值 |
|------|------|
| `--border-color` | `#e5e7eb` |
| `--border-color-light` | `#f0f1f3` |

### 布局

| 令牌 | 值 |
|------|------|
| `--sidebar-width` | `380px`（默认值，可拖拽调整 280-800px） |
| `--header-height` | `64px` |

### 圆角

| 令牌 | 值 |
|------|------|
| `--border-radius-small` | `8px` |
| `--border-radius-base` | `12px` |
| `--border-radius-large` | `16px` |

### 阴影

| 令牌 | 值 |
|------|------|
| `--box-shadow-light` | `0 1px 3px rgba(0,0,0,0.04)` |
| `--box-shadow-base` | `0 2px 8px rgba(0,0,0,0.06)` |
| `--box-shadow-dark` | `0 4px 16px rgba(0,0,0,0.1)` |

### 微信风格专用颜色

- **发送的消息气泡**：`background: #95ec69`，`color: #1f2937`，无边框
- **接收的消息气泡**：`background: #ffffff`，`color: #1f2937`，`border: 1px solid #ececec`
- **发送按钮**：`background: #07c160`（微信绿）

---

## 12. 工具模块

### `storage.ts` — LocalStorage 封装
- 所有键名自动添加 `chat_` 前缀，避免冲突
- 自动 JSON 序列化/反序列化
- 方法：`set<T>(key, value)`、`get<T>(key, defaultValue?)`、`remove(key)`、`clear()`
- 使用的键：`chat_token`、`chat_userInfo`、`chat_theme`、`sidebar-width`

### `date.ts` — 日期格式化
- `formatDate(date, format)` — 使用 dayjs 模板格式化
- `formatRelativeTime(date)` — 返回"刚刚"、"5分钟前"、"3小时前"、"MM-DD HH:mm"
- `isToday(date)` — 检查日期是否为今天
- `formatVoiceDuration(seconds)` — 格式化为 `m:ss`
- `formatDuration(seconds)` — 格式化为 `mm:ss`

### `audio.ts` — 音频播放
- `playVoice(url)` — 播放语音消息（停止当前播放）
- `stopVoice()` — 停止当前播放
- `playNotificationSound()` — 播放系统通知提示音 `notice.MP3`（遵守 `soundEnabled` 标志）
- `playRingtone()` — 播放循环来电铃声 `ring.MP3`，返回停止函数
- `setSoundEnabled(v)` — 全局静音开关

### `websocket.ts` — WebSocket 服务（单例模式）
- 通过 `WebSocketService` 类管理
- 单例导出为 `websocketService`
- 自动重连（3 秒延迟）
- 每 30 秒发送心跳 ping
- 7 个回调注册方法，每个都返回取消订阅函数：
  - `onMessage`、`onStatus`、`onGroupMessage`、`onCallSignal`、`onNotification`、`onFriendRequest`、`onFriendRequestHandled`
- 发送方法：`sendMessage`、`sendGroupMessage`、`sendCallSignal`

### `download.ts` — 文件下载辅助
- `downloadChatRecord(friendId, friendName)` — 下载聊天记录为 `.txt`
- `downloadJson(data, filename)` — 导出数据为 JSON 文件

### `notify.ts` — 快捷通知
- 简单封装：`notify.success(msg)`、`.warning(msg)`、`.error(msg)`、`.info(msg)`

### `useResizable.ts` — 侧边栏拖拽调整大小
- 可配置最小/最大/默认宽度
- 持久化到 `localStorage`
- 返回 `sidebarWidth`、`isResizing`、`startResize`

### `useAuth.ts` — 认证组合式函数
- `login(username, password)` — 调用 API，设置令牌，导航至 `/`
- `register(username, password, nickname)` — 调用 API，导航至 `/login`
- `logout()` — 显示确认对话框，清除状态，导航至 `/login`

### `useWebRTC.ts` — P2P WebRTC
- `createOffer()` / `handleOffer()` / `handleAnswer()` — SDP 协商
- `addIceCandidate()` — ICE 候选处理
- `hangup()` — 清理资源

### `useCallSignal.ts` — 通话信令
- `sendOffer()`、`sendAnswer()`、`sendIceCandidate()`、`sendHangup()`
- 全部通过 `websocketService.sendCallSignal()` 中继

---

## 消息类型参考

| 代码 | 类型 | 说明 | 显示组件 |
|------|------|------|----------|
| 1 | 文本 | 纯文本消息 | MessageBubble 中的 `<span>` |
| 2 | 图片 | 图片消息（可预览） | MessageBubble 中的 `<el-image>` |
| 3 | 表情 | 表情贴纸（80×80px） | MessageBubble 中的 `<el-image>` |
| 4 | 语音 | 语音录音 | `<VoiceMessage>` 组件 |

## 撤回时限

消息可在发送后 **2 分钟** 内撤回。超时后撤回按钮隐藏。`canRecall` 计算属性检查 `Date.now() - sendTime <= 2 * 60 * 1000`。

---

## 音频资源

| 文件 | 路径 | 用途 |
|------|------|------|
| `notice.MP3` | `src/assets/audio/notice.MP3` | 系统通知提示音（以 50% 音量播放） |
| `ring.MP3` | `src/assets/audio/ring.MP3` | 来电铃声（循环播放，80% 音量） |

---

*本文档基于 chat-frontend 项目源代码分析生成。关于后端 API 实现细节，请参阅 Spring Boot 后端代码库。*
