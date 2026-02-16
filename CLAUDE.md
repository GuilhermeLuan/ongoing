# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack subscription management platform built as a **monorepo**:
- **Backend**: Spring Boot 4 REST API with Java 25, Spring Data JPA, PostgreSQL, and Flyway migrations
- **Frontend**: Next.js 14 application with TypeScript, Tailwind CSS, landing page and dashboard

## Monorepo Structure

```
ongoing/
├── backend/          # Spring Boot REST API (port 6969)
├── frontend/         # Next.js application (port 3000)
├── docs/             # Project documentation
└── docker-compose.yaml
```

## Build and Run Commands

### Infrastructure

```bash
# Start PostgreSQL via Docker Compose
docker-compose up -d
```

### Backend Commands

All backend commands must be run from the `backend/` directory:

```bash
cd backend

# Build the project
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Run the application (dev profile)
./mvnw spring-boot:run
```

### Frontend Commands

All frontend commands must be run from the `frontend/` directory:

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Run production server
npm start

# Lint code
npm run lint
```

## Testing Commands

### Backend Tests

All test commands must be run from the `backend/` directory:

```bash
cd backend

# Run unit tests
./mvnw test

# Run all tests including integration tests
./mvnw verify

# Run a specific test class
./mvnw test -Dtest=SubscriptionsServiceTest

# Run a specific integration test
./mvnw verify -Dit.test=SubscriptionsControllerIT
```

Integration tests use Testcontainers with PostgreSQL and RestAssured. They run with `2C` forked JVMs in parallel.

### Frontend Tests

```bash
cd frontend

# Run linter
npm run lint
```

## Architecture

### Backend Architecture

**Layered architecture** with package-by-feature organization (in `backend/src/`):

- `subscriptions/` - Core subscription management module
    - `SubscriptionsController` - REST endpoints (CRUD)
    - `SubscriptionsService` - Business logic, billing date calculations
    - `SubscriptionsRepository` - Spring Data JPA interface
    - `SubscriptionsMapper` - MapStruct mapper for DTO conversion
    - `dto/` - Request/Response DTOs (Java records)
    - `entities/` - JPA entities and enums

- `exception/` - Global exception handling
    - `GlobalExceptionHandler` - Centralized error responses
    - `BadRequestException` - Validation failures
    - `NotFoundException` - Resource not found

- `status/` - Application health endpoint

### Frontend Architecture

**Next.js App Router** structure (in `frontend/src/`):

- `app/` - Next.js app directory
    - `(marketing)/` - Public marketing pages (landing page)
    - `(app)/` - Authenticated application pages (dashboard, subscriptions)
    - `layout.tsx` - Root layout
    - `globals.css` - Global styles

- `components/` - React components
    - `layout/` - Header, Footer
    - `sections/` - Landing page sections (Hero, Features, Pricing, etc)
    - `app/` - Dashboard components (Sidebar, StatCard, SubscriptionCard, etc)
    - `shared/` - Reusable components (Logo, Cards, etc)
    - `ui/` - Base UI components (Button, Input, Card, Badge, etc)

- `hooks/` - Custom React hooks
- `lib/` - Utilities and mock data

## Key Patterns

### Backend

- **MapStruct** for DTO-to-entity mapping (interfaces in `*Mapper.java`)
- **Flyway** migrations in `backend/src/main/resources/db/migration/`
- **Testcontainers** for integration tests - extend `BaseIntegrationTest`
- **Virtual threads** enabled via application.yaml
- **Profiles**: `dev` (auto-DDL, debug logging), `production` (SSL, validate DDL)

### Frontend

- **Next.js App Router** for routing and layouts
- **TypeScript** for type safety
- **Tailwind CSS** for styling with custom design system
- **Component organization**: UI primitives, shared components, feature-specific components
- **Mock data** for development (in `lib/mock-data.ts`)

## API Base

Server runs on port `6969`. All endpoints prefixed with `/api/v1/`.

## Environment Setup

### Backend Environment

Copy `backend/.envTemplate` to `backend/.env` and configure PostgreSQL credentials:

```env
ACTIVE_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=local_db
DB_USER=local_user
DB_PASSWORD=local_password
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://ongoing.up.railway.app
```

The `ACTIVE_PROFILE` variable controls which Spring profile is used.

### Frontend Environment

Create `frontend/.env.local` for local environment variables:

```env
NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1
```

## Additional Notes

For every project, write a detailed FOR-Guilherme.md file that explains the whole project in plain language.

Explain the technical architecture, the structure of the codebase and how the various parts are connected, the
technologies used, why we made these technical decisions, and lessons I can learn from it (this should include the bugs
we ran into and how we fixed them, potential pitfalls and how to avoid them in the future, new technologies used, how
good engineers think and work, best practices, etc).

It should be very engaging to read; don't make it sound like boring technical documentation/textbook. Where appropriate,
use analogies and anecdotes to make it more understandable and memorable.

**IMPORTANTE:** Sempre que fizermos modificações relevantes no projeto (novas features, bugs corrigidos, mudanças de
arquitetura, lições aprendidas), atualize o FOR-Guilherme.md para manter a documentação sincronizada com o código.
