# 🚀 Suggested Commands

## 🛠️ Build & Run
- **Start All (Full Stack)**: `npm run watch` (Starts Vite dev server and Spring Boot backend)
- **Start Backend**: `npm run backend:start` (Maven run with skip NPM)
- **Start Frontend**: `npm run start` (Vite dev server)
- **Docker Services**: `npm run services:up` (Starts DB, Redis, etc.)

## 🧪 Testing
- **Backend Unit Tests**: `npm run backend:unit:test`
- **Frontend Unit Tests**: `npm test`
- **E2E Tests**: `npm run ci:e2e:server:start` (Requires separate setup)

## 🧹 Linting & Formatting
- **Lint**: `npm run lint`
- **Lint Fix**: `npm run lint:fix`
- **Prettier Check**: `npm run prettier:check`
- **Prettier Format**: `npm run prettier:format`

## 📦 Database
- **Liquibase Diff**: `./mvnw liquibase:diff`
- **Liquibase Update**: `./mvnw liquibase:update`
