# Campus Market — 校园二手交易平台

Monorepo: Vue 3 + Spring Boot 全栈项目，用于学习全栈开发。

## Tech Stack

| Layer | Tech |
|---|---|
| Frontend | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios |
| Backend | Spring Boot 3.4.x + MyBatis-Plus 3.5.x + JWT |
| Database | MySQL 8.0 |
| Build | Maven 3.9+ / Node.js 22+ |

## Project Structure

```
Campus-market/
├── frontend/          # Vue 3 SPA (port 5173)
├── backend/           # Spring Boot (port 8080)
├── init.sql           # DB schema + seed data
└── DESIGN.md          # Full architecture spec
```

## Quick Start

```bash
# Backend
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend && npm install && npm run dev
```

- API: `http://localhost:8080/api`
- Frontend: `http://localhost:5173`

## Database

MySQL 8.0.34 @ localhost:3306, database `campus_market`, root/123456.

## Development Rules

- **Each phase follows 讲→做→验** (explain → implement → verify)
- **All pages must cover 4 states**: loading (skeleton), empty (empty state + CTA), error (error message + retry), success (data display)
- **API response format**: `{ code, message, data }`, pagination: `{ records, total, page, size }`
- **API prefix**: `/api`
- **Auth**: JWT in `Authorization: Bearer <token>` header
- **Verify every step** before moving on

## Key Files

- `DESIGN.md` — full architecture, API design, phases, DB schema
- `backend/src/main/resources/application-dev.yml` — local DB config
- `frontend/src/router/index.ts` — route definitions
- `frontend/src/utils/request.ts` — axios instance with JWT interceptor
