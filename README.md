# Campus Market — 校园二手交易平台

Vue 3 + Spring Boot 全栈项目，校园版二手交易市场。

## Tech Stack

| Layer | Tech |
|------|------|
| Frontend | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios |
| Backend | Spring Boot 3.4.x + MyBatis-Plus 3.5.x + JWT |
| Database | MySQL 8.0 |
| Docs | SpringDoc OpenAPI (Swagger UI) |

## Quick Start

### Prerequisites

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0

### 1. Database

Create database and seed data:

```bash
mysql -u root -p < init.sql
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API runs at `http://localhost:8080/api`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs at `http://localhost:5173`

## Project Structure

```
Campus-market/
├── frontend/          # Vue 3 SPA
│   ├── src/
│   │   ├── api/       # Axios request modules
│   │   ├── components/# Shared components
│   │   ├── layouts/   # Page layouts
│   │   ├── router/    # Vue Router config
│   │   ├── stores/    # Pinia state management
│   │   ├── types/     # TypeScript interfaces
│   │   ├── utils/     # Axios instance, constants
│   │   └── views/     # Page components
│   └── vite.config.ts
├── backend/           # Spring Boot
│   └── src/main/java/com/campusmarket/
│       ├── auth/      # JWT authentication
│       ├── user/      # User profile
│       ├── product/   # Product CRUD
│       ├── category/  # Product categories
│       ├── favorite/  # User favorites
│       ├── file/      # Image upload
│       └── common/    # Config, exception, response
├── init.sql           # DB schema + seed data
└── DESIGN.md          # Full architecture spec
```

## Features

- User registration / login (JWT)
- Browse products by category, keyword search, sort
- Product detail with image carousel
- Publish / edit / delete products with multi-image upload
- Favorites (add / remove / batch check)
- User profile with avatar upload
- Route guards (auth required pages)
- Responsive layout (mobile friendly)
- Swagger API docs at `/api/swagger-ui.html`

## API Docs

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

All endpoints return `{ code, message, data }`. Pagination returns `{ records, total, page, size }`.
