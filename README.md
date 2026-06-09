# 💬 在线聊天系统 (Online Chat System)

一个基于前后端分离架构的实时在线聊天系统，支持文字聊天、音视频通话、好友管理、群组聊天等功能。

## ✨ 主要功能

- **消息聊天** — 基于 WebSocket 的实时文字消息，支持私聊和群聊
- **音视频通话** — 基于 mediasoup SFU 的多人音视频通话，支持屏幕共享
- **好友管理** — 好友添加/删除、好友申请处理、在线状态查看
- **群组管理** — 创建群组、邀请成员、群组设置
- **用户印象** — 好友之间可以互相添加印象/评价
- **表情系统** — 聊天中发送表情
- **系统通知** — 系统级通知推送（新好友申请、群组邀请等）
- **管理后台** — 用户管理、数据统计等管理功能
- **文件上传** — 支持图片等文件上传（阿里云 OSS）

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 3.5 + Java 21 |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7 |
| **ORM** | MyBatis-Plus |
| **实时通信** | WebSocket (Spring) |
| **音视频** | mediasoup SFU + Socket.io |
| **前端框架** | Vue 3.5 + TypeScript 6.0 |
| **构建工具** | Vite 8 |
| **UI 组件库** | Element Plus |
| **状态管理** | Pinia |
| **图表** | ECharts 6 |
| **部署** | Docker Compose + PM2 |

## 📁 项目结构

```
├── chat-backend/          # Spring Boot 后端
│   └── src/main/java/com/chat/chat_backend/
│       ├── modules/       # 业务模块 (user, friend, group, message, ...)
│       ├── websocket/     # WebSocket 实时通信
│       ├── config/        # Spring 配置
│       └── interceptor/   # 拦截器
├── chat-frontend/         # Vue 3 前端
│   └── src/
│       ├── views/         # 页面 (auth, chat, profile, admin)
│       ├── components/    # 组件 (chat, call, friend, group, rtc, ...)
│       ├── api/           # API 请求层
│       ├── router/        # 路由配置
│       ├── stores/        # Pinia 状态管理
│       └── utils/         # 工具函数
├── webrtc-sfu/            # mediasoup 音视频 SFU 服务
├── MYSQL/                 # 数据库初始化 SQL
├── docker-compose.yml     # Docker 编排
├── docker.env             # Docker 环境变量
└── ecosystem.config.js    # PM2 配置
```

## 🚀 快速开始

### 前置要求

- Java 21+
- Node.js 22+
- MySQL 8.0
- Redis 7
- Docker & Docker Compose（可选）

### Docker Compose 一键部署（推荐）

```bash
# 1. 克隆项目
git clone git@github.com:ddyy666s/online-chat-system.git
cd online-chat-system

# 2. 配置环境变量（按需修改）
cp .env.example docker.env
# 编辑 docker.env 填入自己的配置

# 3. 启动所有服务
docker compose up -d
```

服务启动后：
- 前端页面：http://localhost:8081
- 后端 API：http://localhost:8080
- WebRTC SFU：http://localhost:3000

### PM2 部署（生产环境）

```bash
# 安装 PM2
npm install -g pm2

# 配置并启动
pm2 start ecosystem.config.js
pm2 save
pm2 startup
```

### 本地开发

**后端：**
```bash
cd chat-backend
./mvnw spring-boot:run
```

**前端：**
```bash
cd chat-frontend
npm install
npm run dev
```

**WebRTC SFU：**
```bash
cd webrtc-sfu
npm install
cp env.example .env
node server.js
```

## ⚙️ 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | `123456` |
| `MYSQL_DATABASE` | 数据库名 | `chat_db` |
| `JWT_SECRET` | JWT 签名密钥 | — |
| `REDIS_HOST` | Redis 地址 | `redis` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `SERVER_PORT` | 后端端口 | `8080` |
| `ALIYUN_OSS_*` | 阿里云 OSS 配置 | — |

> ⚠️ **安全提示**：请勿将包含真实密钥的 `.env` / `docker.env` 文件提交到仓库。使用 `.env.example` 作为模板。

## 📄 License

MIT
