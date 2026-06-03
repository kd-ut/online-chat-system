# WebChat - Complete API Documentation & Project Architecture

> **Version**: 1.0 | **Last Updated**: 2026-06-03 | **Tech Stack**: Vue 3 + TypeScript + Element Plus + Pinia

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Deployment Architecture](#2-deployment-architecture)
3. [Frontend Architecture](#3-frontend-architecture)
4. [Component Tree](#4-component-tree)
5. [Routing Design](#5-routing-design)
6. [State Management](#6-state-management)
7. [WebSocket Protocol](#7-websocket-protocol)
8. [HTTP API Reference](#8-http-api-reference)
   - [8.1 User Module](#81-user-module)
   - [8.2 Friend Module](#82-friend-module)
   - [8.3 Message Module](#83-message-module)
   - [8.4 Group Module](#84-group-module)
   - [8.5 Emoji Module](#85-emoji-module)
   - [8.6 Notification Module](#86-notification-module)
   - [8.7 Impression Module](#87-impression-module)
   - [8.8 Admin Module](#88-admin-module)
9. [TypeScript Type Definitions](#9-typescript-type-definitions)
10. [WebRTC / SFU Architecture](#10-webrtc--sfu-architecture)
11. [CSS Design Tokens](#11-css-design-tokens)
12. [Utility Modules](#12-utility-modules)

---

## 1. Project Overview

WebChat is a full-stack real-time chat application designed after WeChat's UX patterns. It supports:

- **Private chat** (text, image, emoji, voice messages)
- **Group chat** with member management, muting, and notice broadcasting
- **Voice & video calls** via WebRTC (SFU architecture for multi-party video)
- **Message recall** (within 2 minutes of sending)
- **Emoji system** (built-in system emoji + user-uploaded custom emoji)
- **Friend system** (friend requests, grouping, remarks, online status)
- **Impression system** (public comments/ratings between users)
- **Admin dashboard** (user management, message audit, statistics, notification broadcasting)
- **Chat history download**

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Vue 3.5 (Composition API + `<script setup>`) |
| Language | TypeScript 6.0 |
| Build Tool | Vite 8.0 |
| UI Library | Element Plus 2.8 |
| State Management | Pinia 3.0 |
| HTTP Client | Axios 1.7 |
| Routing | Vue Router 5.0 |
| WebSocket | Native WebSocket API |
| Real-time Audio/Video | mediasoup-client 3.20 + Socket.IO 4.8 (SFU signaling) |
| Charts | ECharts 6.0 (admin dashboard) |
| Date Handling | Day.js 1.11 |
| Backend | Spring Boot (Java) |
| Database | MySQL 8.0 + Redis 7 |
| Containerization | Docker Compose |
| Process Manager | PM2 |
| Object Storage | Alibaba Cloud OSS |

---

## 2. Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    PM2 Process Manager                    │
│                                                          │
│  ┌──────────────────┐  ┌──────────────────────────────┐ │
│  │  webchat-stack   │  │        webrtc-sfu            │ │
│  │  (Docker Compose)│  │  (Node.js, port 3000)        │ │
│  │                  │  │  mediasoup SFU server         │ │
│  └──────┬───────────┘  └──────────────────────────────┘ │
└─────────┼────────────────────────────────────────────────┘
          │
          │ Docker Compose
          │
    ┌─────┴───────────────────────────────────┐
    │          Docker Network                  │
    │                                          │
    │  ┌──────────┐  ┌──────────┐  ┌────────┐ │
    │  │  MySQL   │  │  Redis   │  │ Backend│ │
    │  │  :3306   │  │  :6379   │  │ :8080  │ │
    │  └──────────┘  └──────────┘  └───┬────┘ │
    │                                   │      │
    │  ┌────────────────┐              │      │
    │  │   Frontend     │              │      │
    │  │  (Nginx :80)   │──────────────┘      │
    │  │  exposed :8081 │                     │
    │  └────────────────┘                     │
    └──────────────────────────────────────────┘
```

### Nginx Routing (inside Frontend Container)

| Path | Target | Notes |
|------|--------|-------|
| `/` | Static files (`/usr/share/nginx/html`) | SPA fallback to `index.html` |
| `/api/` | `http://backend:8080/` | API proxy (strips `/api` prefix) |
| `/uploads/` | `http://backend:8080` | File serving proxy |
| `/ws` | `http://backend:8080` | WebSocket upgrade proxy |
| `/socket.io/` | `http://webrtc-sfu:3000` | SFU signaling proxy |

### Environment Variables

| Variable | Where | Purpose |
|----------|-------|---------|
| `VITE_API_BASE_URL` | `.env.development` only | Dev API base URL |
| `VITE_WS_URL` | `.env.development` only | Dev WebSocket URL; **NOT in production** (auto-derived from `location.host`) |
| `VITE_RTC_SOCKET_URL` | Build time | SFU Socket.IO URL |
| `VITE_TURN_URL` | Build time (optional) | TURN server for WebRTC |
| `VITE_TURN_USERNAME` | Build time (optional) | TURN username |
| `VITE_TURN_CREDENTIAL` | Build time (optional) | TURN credential |

---

## 3. Frontend Architecture

### Directory Structure

```
src/
├── api/                     # HTTP API layer (per-module files)
│   ├── request.ts           # Axios instance, interceptors
│   ├── admin/index.ts       # Admin APIs
│   ├── emoji/index.ts       # Emoji APIs
│   ├── friend/index.ts      # Friend APIs
│   ├── group/index.ts       # Group APIs
│   ├── impression/index.ts  # Impression APIs
│   ├── message/index.ts     # Message APIs
│   ├── notification/index.ts# Notification APIs
│   └── user/index.ts        # User/Auth APIs
├── assets/
│   ├── audio/               # MP3 files (notice, ringtone)
│   ├── images/              # Static images (logo, default avatar)
│   └── styles/
│       ├── variables.css    # CSS custom properties (design tokens)
│       ├── reset.css        # CSS reset + base styles
│       └── main.css         # Global styles + Element Plus overrides
├── components/
│   ├── call/                # Voice/Video call UI
│   ├── chat/                # Private chat components
│   ├── common/              # Shared components (dialogs, loading...)
│   ├── communication/       # Emoji picker, toolbar, recording
│   ├── friend/              # Friend list items, add friend dialog
│   ├── group/               # Group chat components
│   ├── impression/          # Impression board
│   ├── message/             # Chat window, message box
│   ├── messageBox/          # Notification list
│   ├── messageBubble/       # Message bubble variants
│   ├── rtc/                 # RTC call dialog
│   └── user/                # Mini profile card
├── composables/             # Vue composables (reusable logic)
│   ├── useAuth.ts           # Login/register/logout
│   ├── useCallSignal.ts     # WebRTC call signaling
│   ├── useClickOutside.ts   # (placeholder)
│   ├── useMessage.ts        # ElMessage wrapper
│   ├── usePagination.ts     # (placeholder)
│   ├── useResizable.ts      # Sidebar drag-resize
│   ├── useWebRTC.ts         # Peer-to-peer WebRTC
│   └── useWebSocket.ts      # WebSocket connection
├── router/
│   ├── index.ts             # Router instance + guards
│   └── routes.ts            # Route definitions
├── stores/                  # Pinia stores
│   ├── appStore.ts          # Global app state (theme, sidebar)
│   ├── friendStore.ts       # Friend list + request state
│   ├── messageStore.ts      # Unread count state
│   ├── rtcStore.ts          # RTC/SFU state (mediasoup)
│   ├── userStore.ts         # Auth token + user info
│   └── index.ts             # Re-export barrel
├── types/                   # Shared TypeScript type definitions
│   ├── admin.d.ts
│   ├── api.d.ts
│   ├── friend.d.ts
│   ├── impression.d.ts
│   ├── message.d.ts
│   ├── user.d.ts
│   └── index.ts
├── utils/
│   ├── audio.ts             # Audio playback utilities
│   ├── date.ts              # Date formatting (dayjs)
│   ├── download.ts          # File download helpers
│   ├── notify.ts            # Simple notification wrapper
│   ├── storage.ts           # LocalStorage wrapper with prefix
│   └── websocket.ts         # WebSocket service singleton
├── views/
│   ├── admin/               # Admin dashboard pages
│   ├── auth/                # Login + Register pages
│   ├── chat/                # Main chat view
│   ├── layout/              # Main layout + sidebar
│   └── profile/             # User profile page
├── App.vue                  # Root component
└── main.ts                  # Entry point
```

### HTTP Request Flow

```
Component → API function → Axios instance (/api/request.ts)
    │                            │
    │  Request interceptor       │  Injects Bearer token from userStore
    │                            │
    ▼                            ▼
  Backend ← Nginx proxy ← Axios sends to /api/* (baseURL: '/api')
    │
    │  Response interceptor
    │  ├─ code === 200 → return data directly
    │  ├─ code === 1005 → token expired → force logout
    │  └─ other → show error ElMessage
    ▼
  Component receives typed response
```

### Authentication

- JWT-based authentication
- Token stored in `localStorage` with `chat_` prefix
- Axios request interceptor attaches `Authorization: Bearer <token>` to all requests
- Route guard checks `meta.requiresAuth` and redirects to `/login`
- Token expiry (code 1005) or HTTP 401 triggers automatic logout

---

## 4. Component Tree

```
App.vue
├─ RtcCallDialog (global, always mounted)
└─ <router-view>
    │
    ├─ LoginView
    │   └─ AnimatedBackground
    │
    ├─ RegisterView
    │
    ├─ ProfileView
    │
    ├─ AdminView
    │   ├─ AdminSidebar
    │   ├─ AdminHeader
    │   ├─ StatsCards
    │   ├─ StatsChart
    │   ├─ UserManage
    │   ├─ MessageAudit
    │   └─ NotificationManage
    │
    └─ MainLayout
        ├─ Sidebar
        │   ├─ SidebarHeader
        │   ├─ SidebarTabs
        │   ├─ FriendList
        │   │   └─ FriendGroup
        │   │       └─ FriendItem
        │   ├─ GroupList
        │   ├─ RequestList
        │   │   └─ FriendRequestItem
        │   └─ ImpressionBoard
        │       └─ ImpressionList
        │           └─ ImpressionItem
        ├─ resize-handle
        └─ right-panel
            ├─ Header
            │   ├─ bell-badge
            │   ├─ user-dropdown
            │   └─ MessageBox
            │       ├─ MessageList
            │       │   └─ MessageItem
            │       └─ NotificationDialog
            └─ Content
                └─ ChatView
                    ├─ ChatWindow
                    │   ├─ ChatHeader
                    │   ├─ MessageList
                    │   │   └─ MessageBubble
                    │   │       └─ VoiceMessage
                    │   ├─ MessageInput
                    │   │   ├─ CommunicationBar
                    │   │   │   ├─ Toolbar
                    │   │   │   ├─ RecordingTip
                    │   │   │   └─ EmojiPicker
                    │   │   │       └─ EmojiGrid
                    │   │   └─ (textarea + buttons)
                    │   ├─ CallDialog
                    │   └─ DownloadDialog
                    └─ GroupChatWindow
                        ├─ GroupChatHeader
                        ├─ GroupMessageList
                        ├─ GroupMessageInput
                        ├─ GroupManagementDialog
                        ├─ GroupMembersDialog
                        ├─ GroupNoticeDialog
                        └─ InviteFriendDialog
```

---

## 5. Routing Design

| Path | Name | Component | Auth | Admin | Title |
|------|------|-----------|------|-------|-------|
| `/login` | Login | LoginView | No | No | Login |
| `/register` | Register | RegisterView | No | No | Register |
| `/` | Main | MainLayout | Yes | No | Chat |
| `/profile` | Profile | ProfileView | Yes | No | Profile |
| `/admin` | Admin | AdminView | Yes | Yes | Admin Panel |

**Query Parameters on Main Route:**

| Parameter | Type | Purpose |
|-----------|------|---------|
| `friendId` | number | Opens private chat with specified friend |
| `groupId` | number | Opens group chat with specified group |

**Route Guards:**
- `requiresAuth !== false` → must be logged in (redirect to `/login`)
- `requiresAdmin === true` → must have `role === 'admin'` (redirect to `/`)

---

## 6. State Management

### Pinia Stores

#### `userStore` (user)
| State | Type | Description |
|-------|------|-------------|
| `token` | `string \| null` | JWT token |
| `userInfo` | `UserInfo \| null` | Current user info |

| Method | Signature | Description |
|--------|-----------|-------------|
| `setToken` | `(token: string) => void` | Save token to state + localStorage |
| `setUserInfo` | `(info: UserInfo) => void` | Save user info to state + localStorage |
| `logout` | `() => void` | Clear token, user info, localStorage |
| `isLoggedIn` | `() => boolean` | Check if logged in |
| `isAdmin` | `() => boolean` | Check if admin role |

#### `friendStore` (friend)
| State | Type | Description |
|-------|------|-------------|
| `friendList` | `FriendGroupVO[]` | Friends grouped by category |
| `friendRequests` | `FriendRequestVO[]` | Pending friend requests |

| Method | Description |
|--------|-------------|
| `loadFriendList()` | Fetch friend list from API |
| `loadFriendRequests()` | Fetch friend requests from API |
| `getGroupNames()` | Get unique group names |
| `getFriendById(userId)` | Find friend across all groups |
| `clearUnreadForFriend(friendId)` | Reset unread count to 0 |
| `incrementUnreadForFriend(friendId)` | Increment unread count by 1 |
| `updateFriendOnlineStatus(userId, isOnline)` | Update online status |

#### `messageStore` (message)
| State | Type | Description |
|-------|------|-------------|
| `unreadCount` | `UnreadCountVO \| null` | Unread message aggregation |

| Method | Description |
|--------|-------------|
| `loadUnreadCount()` | Fetch unread counts from API |
| `clearUnreadForFriend(friendId)` | Remove unread entries for a friend |

#### `appStore` (app)
| State | Type | Description |
|-------|------|-------------|
| `sidebarCollapsed` | `boolean` | Sidebar collapsed state |
| `theme` | `string` | Current theme |
| `globalLoading` | `boolean` | Full-screen loading |

#### `rtcStore` (rtc) — Mediasoup-based video calling
| State | Description |
|-------|-------------|
| `visible` | Call dialog visibility |
| `roomId` / `roomTitle` | Current room info |
| `members` | Room members |
| `remoteMedias` / `remoteVideos` / `remoteAudios` | Remote media streams |
| `localStream` | Local media stream |
| `isJoined` / `isPublishing` / `isMicMuted` / `isCameraOff` | Call state flags |

---

## 7. WebSocket Protocol

### Connection

The WebSocket connects to the same host as the HTTP server using the path `/ws` with the JWT token as a query parameter.

```
URL: ws://<host>/ws?token=<jwt_token>
     or wss://<host>/ws?token=<jwt_token> (HTTPS)
```

**Auto-reconnect**: 3-second delay, unlimited retries.  
**Heartbeat**: sends `{"type": "ping"}` every 30 seconds.

### Client → Server Messages

#### Send Private Message
```json
{
  "type": "message",
  "toUserId": 123,
  "content": "Hello!",
  "messageType": 1,
  "duration": 5
}
```
- `messageType`: `1` = text, `2` = image, `3` = emoji, `4` = voice
- `duration`: only for voice messages (type 4)

#### Send Group Message
```json
{
  "type": "group_message",
  "groupId": 456,
  "content": "Hello group!",
  "messageType": 1
}
```

#### Send Call Signal
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

#### Heartbeat
```json
{"type": "ping"}
```

### Server → Client Messages

Each message has a `type` field used for routing to the appropriate callback:

| `type` Value | Description | Routed To |
|-------------|-------------|-----------|
| `message` | Private chat message + impressions | `messageCallbacks` |
| `status` | Online/offline status updates | `statusCallbacks` |
| `group_message` | Group chat message | `groupMessageCallbacks` |
| `call` | Call signaling (WebRTC/SFU) | `callSignalCallbacks` |
| `notification` | System notification from admin | `notificationCallbacks` |
| `friend_request` | New friend request received | `friendRequestCallbacks` |
| `friend_request_handled` | Friend request accepted/rejected | `friendRequestHandledCallbacks` |

### Callback Registration (with Cleanup)

Every `on*` method returns an **unsubscribe function** to prevent memory leaks:

```typescript
const unsubscribe = websocketService.onMessage((data) => { ... })
// Later:
unsubscribe()  // removes this callback
```

---

## 8. HTTP API Reference

### Base Configuration

- **Base URL**: `/api` (proxied to backend by Nginx)
- **Content-Type**: `application/json` (except file uploads: `multipart/form-data`)
- **Auth**: `Authorization: Bearer <token>` header
- **Standard Response**:
  ```json
  { "code": 200, "message": "success", "data": ... }
  ```
- **Error Codes**: `200` = success, `1005` = token expired, others = business error

---

### 8.1 User Module

#### `POST /user/login`
Login with username and password.

**Request Body:**
```json
{ "username": "string", "password": "string" }
```

**Response:** `LoginResponse`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "john",
    "nickname": "John Doe",
    "avatar": "https://...",
    "signature": "Hello world",
    "role": "user"
  }
}
```

#### `POST /user/register`
Register a new account.

**Request Body:**
```json
{ "username": "string", "password": "string", "nickname": "string" }
```

#### `GET /user/me`
Get current authenticated user's profile.

**Response:** `UserInfo`

#### `PUT /user/profile`
Update current user's profile.

**Request Body:**
```json
{ "nickname": "string?", "signature": "string?" }
```

**Response:** `UserInfo`

#### `POST /user/avatar`
Upload avatar image.

**Request:** `multipart/form-data` with field `file` (image)

**Response:** `string` (avatar URL)

#### `GET /user/{userId}`
Get another user's public profile.

**Response:** `UserInfo`

---

### 8.2 Friend Module

#### `GET /friend/search?keyword=xxx`
Search users by keyword (username/nickname).

**Response:** `SearchUserVO[]`
```json
[{
  "userId": 2, "nickname": "Alice", "avatar": null,
  "signature": "Hello", "remark": null, "isOnline": true
}]
```

#### `POST /friend/request`
Send a friend request.

**Request Body:**
```json
{ "toUserId": 2, "message": "Let's be friends!" }
```

#### `GET /friend/requests`
Get all pending friend requests (incoming).

**Response:** `FriendRequestVO[]`

#### `PUT /friend/request/{requestId}`
Accept or reject a friend request.

**Request Body:**
```json
{ "status": 1 }
```
`status`: `1` = accept, `2` = reject

#### `GET /friend/list`
Get the current user's friend list, grouped by category.

**Response:** `FriendGroupVO[]`
```json
[{
  "groupName": "My Friends",
  "friends": [{
    "id": 1, "userId": 2, "nickname": "Alice", "avatar": null,
    "signature": "Hello", "remark": null, "groupName": "My Friends",
    "isOnline": true, "unreadCount": 0
  }]
}]
```

#### `DELETE /friend/{friendId}`
Delete a friend relationship.

#### `PUT /friend/{friendId}/group`
Move a friend to a different group.

**Request Body:**
```json
{ "groupName": "Close Friends" }
```

#### `PUT /friend/{friendId}/remark?remark=xxx`
Update a friend's remark/alias.

**Query Parameter:** `remark` (string)

---

### 8.3 Message Module

#### `GET /message/history/{friendId}?page=1&size=20`
Get chat history with a specific friend (paginated).

**Response:** `PageResult<MessageVO>`
```json
{
  "total": 100,
  "records": [{
    "id": 1, "fromUserId": 1, "fromUserNickname": "John",
    "fromUserAvatar": null, "toUserId": 2, "toUserNickname": "Alice",
    "messageType": 1, "content": "Hello!",
    "isRead": true, "isRecalled": false, "sendTime": "2026-06-03T10:00:00"
  }]
}
```

**Message Types:**
| Value | Type |
|-------|------|
| 1 | Text |
| 2 | Image |
| 3 | Emoji |
| 4 | Voice |

#### `GET /message/download/{friendId}?limit=100`
Download chat history as a `.txt` file. Returns a file blob.

**Query Parameter:** `limit` (max number of messages, default 100)

#### `PUT /message/read/{friendId}`
Mark all messages from a friend as read.

#### `GET /message/unread/count`
Get unread message counts across all friends.

**Response:** `UnreadCountVO`
```json
{
  "total": 5,
  "details": [{ "friendId": 2, "friendNickname": "Alice", "friendAvatar": null, "unreadCount": 3 }],
  "messages": [{ "id": 10, "fromUserId": 2, "fromUserNickname": "Alice", "fromUserAvatar": null, "content": "Hi", "sendTime": "...", "messageType": 1 }]
}
```

#### `PUT /message/recall/{messageId}`
Recall a message (within 2-minute window).

#### `POST /message/upload/image`
Upload an image file for sending in chat.

**Request:** `multipart/form-data` with field `file` (image)

**Response:** `string` (image URL)

#### `POST /message/upload/voice`
Upload a voice recording file.

**Request:** `multipart/form-data` with field `file` (audio)

**Response:** `string` (voice file URL)

---

### 8.4 Group Module

#### `POST /group`
Create a new group chat.

**Request Body:**
```json
{
  "name": "Team Chat",
  "avatar": "https://...",
  "notice": "Welcome!",
  "memberIds": [2, 3, 4]
}
```

**Response:** `GroupVO`

#### `GET /group/list`
Get list of groups the current user belongs to.

**Response:** `GroupVO[]`
```json
[{
  "id": 1, "name": "Team Chat", "avatar": null,
  "notice": "Welcome!", "ownerId": 1, "memberCount": 5,
  "unreadCount": 2, "createdAt": "2026-06-01T00:00:00"
}]
```

#### `GET /group/{groupId}`
Get group details.

**Response:** `GroupVO`

#### `GET /group/message/{groupId}?page=1&size=20`
Get group chat history (paginated).

**Response:** `{ total: number, records: GroupMessageVO[] }`

#### `GET /group/{groupId}/members`
Get members of a group.

**Response:** `GroupMemberVO[]`
```json
[{
  "userId": 1, "nickname": "John", "avatar": null,
  "groupNickname": null, "role": 2, "muted": false
}]
```
**Roles:** `0` = member, `1` = admin, `2` = owner

#### `POST /group/invite`
Invite a user to the group.

**Request Body:**
```json
{ "groupId": 1, "userId": 5 }
```

#### `DELETE /group/{groupId}/quit`
Leave a group.

#### `DELETE /group/{groupId}/disband`
Disband a group (owner only).

#### `PUT /group/{groupId}/read`
Clear unread count for a group.

#### `PUT /group/{groupId}/notice`
Update group notice.

**Request Body:**
```json
{ "notice": "New notice text" }
```

#### `PUT /group/{groupId}/member/{memberId}/set-admin`
Promote a member to admin.

#### `PUT /group/{groupId}/member/{memberId}/remove-admin`
Demote an admin to regular member.

#### `DELETE /group/{groupId}/member/{memberId}`
Remove a member from the group (owner/admin only).

#### `PUT /group/{groupId}/member/{memberId}/mute`
Mute a member.

**Request Body:**
```json
{ "minutes": 30 }
```

#### `PUT /group/{groupId}/member/{memberId}/unmute`
Unmute a member.

#### `PUT /group/{groupId}/members/batch-mute`
Batch mute multiple members.

**Request Body:**
```json
{ "memberIds": [2, 3], "minutes": 60 }
```

---

### 8.5 Emoji Module

#### `GET /emoji/system`
Get the built-in system emoji pack.

**Response:** `EmojiVO[]`
```json
[{
  "id": 1, "name": "smile", "url": "https://...",
  "category": "default", "isSystem": true, "createdAt": "..."
}]
```

#### `GET /emoji/user`
Get the current user's uploaded custom emojis.

**Response:** `EmojiVO[]`

#### `POST /emoji/upload`
Upload a custom emoji.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file |
| `name` | String | Emoji name (1-20 chars, alphanumeric or Chinese) |
| `category` | String? | Category (optional) |

**Response:** `EmojiVO`

#### `DELETE /emoji/{emojiId}`
Delete a user's custom emoji.

---

### 8.6 Notification Module

#### `GET /system-notification/unread`
Get unread system notifications (silent — no error toasts).

**Response:**
```json
{
  "total": 2,
  "notifications": [{ "id": 1, "title": "...", "content": "...", "adminId": 1, "adminNickname": "Admin", "createdAt": "..." }]
}
```

#### `PUT /system-notification/read/{notificationId}`
Mark a notification as read.

#### `POST /system-notification/send`
Admin: send a system-wide notification.

**Request Body:**
```json
{ "title": "Notice", "content": "Server will restart at 3:00 AM" }
```

#### `GET /admin/notifications`
Admin: get list of sent notifications (silent).

---

### 8.7 Impression Module

"Impressions" are public comments/ratings that users can leave on each other's profiles.

#### `POST /impression`
Add an impression on another user.

**Request Body:**
```json
{ "toUserId": 2, "content": "Very friendly and helpful!" }
```

#### `GET /impression/to-me`
Get impressions others have left about me.

**Response:** `ImpressionVO[]`

#### `GET /impression/by-me`
Get impressions I have left for others.

**Response:** `ImpressionVO[]`

#### `DELETE /impression/{impressionId}`
Delete an impression.

---

### 8.8 Admin Module

Requires admin role (`user.role === 'admin'`).

#### `GET /admin/stats`
Get dashboard statistics.

**Response:** `StatisticsVO`
```json
{ "totalUsers": 150, "todayActiveUsers": 42, "todayMessages": 320, "onlineUsers": 18 }
```

#### `GET /admin/users?page=1&size=10&keyword=xxx`
Get users list with pagination and optional search.

**Response:** `PageResult<UserManageVO>`

#### `PUT /admin/user/{userId}/status?status=0`
Enable or disable a user account.
`status`: `0` = disabled, `1` = enabled

#### `GET /admin/messages?params...`
Get messages for audit (paginated, filterable).

**Response:** `PageResult<MessageAuditVO>`

---

## 9. TypeScript Type Definitions

### Core Message Types

```typescript
// Message type enum (numeric)
// 1 = TEXT, 2 = IMAGE, 3 = EMOJI, 4 = VOICE

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
  sendTime: string       // ISO date string
  duration?: number      // voice duration in seconds
}
```

### User Types

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

### Friend Types

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
  status: number  // 0=pending, 1=accepted, 2=rejected
  createdAt: string
}
```

### Group Types

```typescript
interface GroupVO {
  id: number; name: string; avatar: string | null
  notice: string | null; ownerId: number
  memberCount: number; unreadCount: number; createdAt: string
}

interface GroupMemberVO {
  userId: number; nickname: string; avatar: string | null
  groupNickname: string | null
  role: number   // 0=member, 1=admin, 2=owner
  muted?: boolean
}

interface GroupMessageVO {
  id: number; groupId: number; fromUserId: number
  fromUserNickname: string; fromUserAvatar: string | null
  content: string; messageType: number; sendTime: string
}
```

### Generic Types

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

## 10. WebRTC / SFU Architecture

### Overview

The project has two calling mechanisms:

| Feature | Technology | Signaling |
|---------|-----------|-----------|
| **Voice Calls (P2P)** | WebRTC peer-to-peer (STUN/TURN) | WebSocket (`type: "call"`) |
| **Video Calls (SFU)** | mediasoup SFU via `webrtc-sfu` service | Socket.IO to port 3000 |

### SFU Video Call Flow

```
Client A ←── Socket.IO ──→ webrtc-sfu (Node.js + mediasoup) ←── Socket.IO ──→ Client B
              │                                                            │
              └─────────────── Media (RTP) via mediasoup ─────────────────┘
```

**RTC Socket.IO Signaling Commands:**

| Command | Direction | Purpose |
|---------|-----------|---------|
| `createRoomInvite` | Client → Server | Create room and invite user |
| `respondRoomInvite` | Client → Server | Accept/reject invite |
| `joinRoom` | Client → Server | Join room, get RTP capabilities |
| `createWebRtcTransport` | Client → Server | Create mediasoup transport |
| `connectTransport` | Client → Server | Connect DTLS transport |
| `produce` | Client → Server | Start sending media |
| `consume` | Client → Server | Start receiving media |
| `resume` | Client → Server | Resume consumer |
| `pauseProducer` | Client → Server | Pause a producer |
| `resumeProducer` | Client → Server | Resume a producer |
| `leaveRoom` | Client → Server | Leave room |
| `endRoom` | Client → Server | End room (creator) |

**Server → Client Events:**

| Event | Purpose |
|-------|---------|
| `incomingInvite` | Someone invited you to a call |
| `inviteResponded` | Your invite was accepted/rejected |
| `roomMembersUpdate` | Member list changed |
| `newProducer` | New media producer available |
| `producerClosed` | Media producer closed |
| `peerLeft` | Peer left room |
| `roomEnded` | Room ended |
| `disconnect` | Socket disconnected |

### Key Configuration

- **SFU Port**: 3000 (Socket.IO signaling)
- **RTC Ports**: 2000-2100 (mediasoup media)
- **STUN**: `stun.l.google.com:19302`
- **TURN**: Optional, configured via `VITE_TURN_URL`/`VITE_TURN_USERNAME`/`VITE_TURN_CREDENTIAL`
- **Rejoin Grace**: 30 seconds (allows page refresh without losing room membership)

---

## 11. CSS Design Tokens

### Color Palette

| Token | Value | Usage |
|-------|-------|-------|
| `--color-primary` | `#5b6abf` | Primary buttons, active states, links |
| `--color-primary-light` | `#8b9cf7` | Hover backgrounds, focus rings |
| `--color-primary-dark` | `#4554a0` | Button hover, emphasis |
| `--color-success` | `#20a39e` | Success messages/badges |
| `--color-warning` | `#f0a050` | Warnings |
| `--color-danger` | `#e87461` | Errors, destructive actions |
| `--color-info` | `#8e99a4` | Info badges |

### Text Colors

| Token | Value | Usage |
|-------|-------|-------|
| `--text-primary` | `#1f2937` | Headings, body text |
| `--text-regular` | `#6b7280` | Secondary text, labels |
| `--text-secondary` | `#9ca3af` | Muted/tertiary text |
| `--text-placeholder` | `#d1d5db` | Input placeholders |

### Background Colors

| Token | Value | Usage |
|-------|-------|-------|
| `--bg-color` | `#f5f6f8` | Page background |
| `--bg-color-white` | `#ffffff` | Card/surface backgrounds |
| `--bg-color-overlay` | `#ffffff` | Dropdown/modal backgrounds |

### Border Colors

| Token | Value |
|-------|-------|
| `--border-color` | `#e5e7eb` |
| `--border-color-light` | `#f0f1f3` |

### Layout

| Token | Value |
|-------|-------|
| `--sidebar-width` | `380px` (default, resizable 280-800px) |
| `--header-height` | `64px` |

### Border Radius

| Token | Value |
|-------|-------|
| `--border-radius-small` | `8px` |
| `--border-radius-base` | `12px` |
| `--border-radius-large` | `16px` |

### Shadows

| Token | Value |
|-------|-------|
| `--box-shadow-light` | `0 1px 3px rgba(0,0,0,0.04)` |
| `--box-shadow-base` | `0 2px 8px rgba(0,0,0,0.06)` |
| `--box-shadow-dark` | `0 4px 16px rgba(0,0,0,0.1)` |

### WeChat-style Specific Colors

- **Sent message bubble**: `background: #95ec69`, `color: #1f2937`, no border
- **Received message bubble**: `background: #ffffff`, `color: #1f2937`, `border: 1px solid #ececec`
- **Send button**: `background: #07c160` (WeChat green)

---

## 12. Utility Modules

### `storage.ts` — LocalStorage Wrapper
- Prefix `chat_` added to all keys to avoid collisions
- Automatic JSON serialization/deserialization
- Methods: `set<T>(key, value)`, `get<T>(key, defaultValue?)`, `remove(key)`, `clear()`
- Keys used: `chat_token`, `chat_userInfo`, `chat_theme`, `sidebar-width`

### `date.ts` — Date Formatting
- `formatDate(date, format)` — format with dayjs template
- `formatRelativeTime(date)` — returns "刚刚", "5分钟前", "3小时前", "MM-DD HH:mm"
- `isToday(date)` — check if date is today
- `formatVoiceDuration(seconds)` — format as `m:ss`
- `formatDuration(seconds)` — format as `mm:ss`

### `audio.ts` — Audio Playback
- `playVoice(url)` — play a voice message (stops any currently playing audio)
- `stopVoice()` — stop current playback
- `playNotificationSound()` — play `notice.MP3` for system notifications (respects `soundEnabled` flag)
- `playRingtone()` — play looping `ring.MP3` for incoming calls, returns stop function
- `setSoundEnabled(v)` — global mute toggle

### `websocket.ts` — WebSocket Service (Singleton)
- Managed via `WebSocketService` class
- Singleton export as `websocketService`
- Auto-reconnect with 3-second delay
- Heartbeat ping every 30 seconds
- 7 callback registration methods, each returning an unsubscribe function:
  - `onMessage`, `onStatus`, `onGroupMessage`, `onCallSignal`, `onNotification`, `onFriendRequest`, `onFriendRequestHandled`
- Send methods: `sendMessage`, `sendGroupMessage`, `sendCallSignal`

### `download.ts` — File Download Helpers
- `downloadChatRecord(friendId, friendName)` — download chat history as `.txt`
- `downloadJson(data, filename)` — export data as JSON file

### `notify.ts` — Quick Notification
- Simple wrapper: `notify.success(msg)`, `.warning(msg)`, `.error(msg)`, `.info(msg)`

### `useResizable.ts` — Sidebar Drag-to-Resize
- Configurable min/max/default width
- Persists to `localStorage`
- Returns `sidebarWidth`, `isResizing`, `startResize`

### `useAuth.ts` — Auth Composable
- `login(username, password)` — calls API, sets token, navigates to `/`
- `register(username, password, nickname)` — calls API, navigates to `/login`
- `logout()` — shows confirmation dialog, clears state, navigates to `/login`

### `useWebRTC.ts` — P2P WebRTC
- `createOffer()` / `handleOffer()` / `handleAnswer()` — SDP negotiation
- `addIceCandidate()` — ICE candidate handling
- `hangup()` — cleanup

### `useCallSignal.ts` — Call Signaling
- `sendOffer()`, `sendAnswer()`, `sendIceCandidate()`, `sendHangup()`
- All relay through `websocketService.sendCallSignal()`

---

## Message Type Reference

| Code | Type | Description | Display Component |
|------|------|-------------|-------------------|
| 1 | Text | Plain text message | `<span>` in MessageBubble |
| 2 | Image | Picture (previewable) | `<el-image>` in MessageBubble |
| 3 | Emoji | Emoji sticker (80×80px) | `<el-image>` in MessageBubble |
| 4 | Voice | Voice recording | `<VoiceMessage>` component |

## Recall Time Limit

Messages can be recalled within **2 minutes** of sending. After that, the recall button is hidden. The `canRecall` computed property checks `Date.now() - sendTime <= 2 * 60 * 1000`.

---

## Audio Assets

| File | Path | Usage |
|------|------|-------|
| `notice.MP3` | `src/assets/audio/notice.MP3` | System notification sound (played at 50% volume) |
| `ring.MP3` | `src/assets/audio/ring.MP3` | Incoming call ringtone (looping, 80% volume) |

---

*Generated from source code analysis of the chat-frontend project. For backend API implementation details, refer to the Spring Boot backend codebase.*
