# WebChat — Online Chat System

A full-stack real-time chat application with WebRTC video/audio calls, group chat, moments (朋友圈), and admin dashboard.

## Tech Stack

| Layer        | Technology                        |
| ------------ | --------------------------------- |
| Frontend     | Vue 3 + TypeScript + Pinia + Vite |
| UI           | Element Plus + Sass               |
| Backend      | Spring Boot 3.2 + Java 17         |
| ORM          | MyBatis-Plus 3.5.5                |
| Database     | MySQL 8.0 + Redis 7               |
| Real-time    | Native WebSocket + Socket.io      |
| RTC          | mediasoup 3.12 (SFU)              |
| File Storage | Local disk / Alibaba Cloud OSS    |
| Deployment   | Docker Compose / PM2              |

## Architecture

```
Browser (Vue 3 SPA)
    │
    ├── Axios ──── HTTP ────► Spring Boot (port 8080)
    │                              │
    │                              ├── JWT Auth (except /ws/**, /user/login, /user/register)
    │                              ├── REST Controllers (User, Friend, Message, Group, etc.)
    │                              ├── MyBatis-Plus → MySQL
    │                              └── Redis (online status, unread counts)
    │
    ├── WebSocket ── ws:// ────► Spring Boot (port 8080/ws)
    │                              │
    │                              ├── Chat messages, typing, read receipts
    │                              └── P2P call signaling (fallback)
    │
    └── Socket.io ────────────► mediasoup SFU (port 3001)
                                   │
                                   ├── JWT auth middleware
                                   ├── Room management
                                   └── WebRTC transport (produce/consume)
```

## Project Structure

```
webchat/
├── chat-frontend/          # Vue 3 SPA
│   ├── src/
│   │   ├── api/            # Axios request modules
│   │   ├── composables/    # Vue composables (useAuth, useChat, useWebRTC, etc.)
│   │   ├── stores/         # Pinia stores (user, app, message, friend, rtc*)
│   │   ├── utils/          # WebSocket client, storage, date, audio, notify
│   │   ├── types/          # TypeScript interfaces
│   │   ├── styles/         # CSS (dark theme)
│   │   ├── views/          # Pages (Login, MainLayout, Chat, Contacts, Moments)
│   │   └── router/         # Vue Router config
│   ├── vite.config.ts
│   └── package.json
│
├── chat-backend/           # Spring Boot REST + WebSocket
│   └── src/main/java/com/chat/chat_backend/
│       ├── controller/     # REST controllers
│       ├── service/        # Service interfaces + implementations
│       ├── mapper/         # MyBatis-Plus mappers
│       ├── websocket/      # WebSocket handler, session manager, signal relay
│       ├── config/         # Spring configs (WebSocket, Redis, MyBatis-Plus, OSS)
│       ├── interceptor/    # JWT interceptor
│       └── common/         # JWT util, Redis util, OSS util, Result, exceptions
│
├── webrtc-sfu/             # mediasoup SFU (Node.js)
│   ├── server.js           # Express + Socket.io + mediasoup worker
│   ├── roomManager.js      # Room and transport lifecycle
│   ├── socketHandler.js    # Socket.io event handlers
│   ├── roomEvents.js       # Call signaling events
│   ├── mediaEvents.js      # Producer pause/resume/close
│   ├── state.js            # In-memory state (Maps)
│   └── config.js           # Codec and transport config
│
├── MYSQL/
│   └── chat_db.sql         # Schema (9 tables + 1 view)
│
├── docker-compose.yml      # Multi-container deployment
├── ecosystem.config.js     # PM2 deployment
└── .env                    # Environment variables
```

## Quick Start

### Prerequisites

- Node.js 18+, Java 17, MySQL 8.0, Redis 7

### Database

```bash
mysql -u root -p < MYSQL/chat_db.sql
```

### Backend

```bash
cd chat-backend
mvn package -DskipTests
java -jar target/chat-backend.jar
```

Access: `http://localhost:8080`

### Frontend

```bash
cd chat-frontend
npm install
npm run dev
```

Access: `http://localhost:5173`

### SFU

```bash
cd webrtc-sfu
npm install
node server.js
```

Access: `ws://localhost:3001`

### Docker (all-in-one)

```bash
docker-compose up -d
```

## Key Features

- **Real-time chat**: WebSocket messaging with typing indicators, read receipts, message recall
- **Audio/Video calls**: SFU-based WebRTC via mediasoup, with P2P fallback
- **Group chat**: Create groups, manage members, ownership transfer
- **Moments (朋友圈)**: Image/text posts, likes, comments, privacy settings
- **Custom emoji**: Upload and use personal emoji
- **File sharing**: Image, file, voice message uploads (local or Alibaba OSS)
- **Friend management**: Search, request, accept/reject, block
- **Notifications**: In-app + desktop browser notifications
- **Admin panel**: Dashboard stats, user management, message audit, system config
- **Dark theme**: Full dark UI with customizable palette

## API Documentation

Detailed docs available in `docs/`:

- `01-overview.md` — Architecture & tech stack
- `02-frontend-foundation.md` — Configs, router, styles, types
- `03-frontend-stores.md` — All Pinia stores
- `04-frontend-api-utils.md` — API modules & utilities
- `05-frontend-composables.md` — Vue composables
- `06-frontend-views.md` — Key views
- `07-backend-foundation.md` — pom.xml, configs, interceptors
- `08-backend-controllers-services.md` — Controllers, services, mappers
- `09-backend-websocket.md` — WebSocket handlers
- `10-sfu-database-devops.md` — SFU, MySQL schema, deployment

## Configuration

Key environment variables (`.env`):

| Variable              | Default          | Description                       |
| --------------------- | ---------------- | --------------------------------- |
| `MYSQL_ROOT_PASSWORD` | `root`           | MySQL password                    |
| `REDIS_PASSWORD`      | (empty)          | Redis password                    |
| `JWT_SECRET`          | (256-bit key)    | JWT signing secret                |
| `SFU_JWT_SECRET`      | `sfu-secret-key` | SFU JWT secret                    |
| `OSS_TYPE`            | `local`          | File storage: `local` or `aliyun` |
| `ANNOUNCED_IP`        | `127.0.0.1`      | SFU public IP for WebRTC          |

## License

MIT
