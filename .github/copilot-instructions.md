# Copilot Instructions - Ongoing

Full-stack subscription management platform built as a **monorepo** with Spring Boot backend and Next.js frontend.

## Monorepo Structure

This is a monorepo with separate backend and frontend directories. **Commands must be run from the appropriate
directory:**

- **Backend commands**: Always `cd backend` first
- **Frontend commands**: Always `cd frontend` first

```
ongoing/
├── backend/          # Spring Boot REST API (port 6969)
├── frontend/         # Next.js application (port 3000)
├── docs/             # Project documentation
└── docker-compose.yaml
```

## Quick Start

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Run backend (in backend/ directory)
cd backend && ./mvnw spring-boot:run

# 3. Run frontend (in frontend/ directory)
cd frontend && npm run dev
```

## Build, Test, and Lint Commands

### Backend (Spring Boot 4 + Java 25)

Run from `backend/` directory:

```bash
# Build
./mvnw clean package
./mvnw clean package -DskipTests  # Skip tests

# Run application
./mvnw spring-boot:run

# Tests
./mvnw test                                    # Unit tests only
./mvnw verify                                  # All tests (unit + integration)
./mvnw test -Dtest=SubscriptionsServiceTest    # Specific test class
./mvnw verify -Dit.test=SubscriptionsControllerIT  # Specific integration test
```

**Integration tests:**

- Use Testcontainers with PostgreSQL
- Use RestAssured for API testing
- Run with `2C` forked JVMs in parallel
- Extend `BaseIntegrationTest` when creating new integration tests

### Frontend (Next.js 14 + TypeScript)

Run from `frontend/` directory:

```bash
# Development
npm install              # Install dependencies
npm run dev              # Start dev server

# Build
npm run build            # Production build
npm start                # Run production build

# Tests & Linting
npm run lint             # ESLint
npm run test             # Run Vitest tests
npm run test:ui          # Vitest UI
npm run test:coverage    # Coverage report
```

## Architecture

### Backend - Package-by-Feature

Base package: `dev.guilhermeluan.ongoing`

**Core modules:**

- `subscriptions/` - Subscription management (CRUD operations, billing calculations)
    - `SubscriptionsController` - REST endpoints
    - `SubscriptionsService` - Business logic
    - `SubscriptionsRepository` - Spring Data JPA
    - `SubscriptionsMapper` - MapStruct DTO mapper
    - `dto/` - Request/Response DTOs (Java records)
    - `entities/` - JPA entities and enums

- `user/` - User management

- `exception/` - Global error handling
    - `GlobalExceptionHandler` - Centralized `@RestControllerAdvice`
    - `BadRequestException` - 400 errors
    - `NotFoundException` - 404 errors

- `status/` - Health check endpoint

**Database:**

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V1__description.sql`, `V2__description.sql`, etc.

### Frontend - Next.js App Router

Base directory: `src/`

```
src/
├── app/                    # Next.js App Router
│   ├── (marketing)/        # Public pages (landing page)
│   ├── (app)/              # Authenticated app pages (dashboard)
│   ├── layout.tsx          # Root layout
│   └── globals.css         # Global styles
├── components/
│   ├── layout/             # Header, Footer
│   ├── sections/           # Landing page sections (Hero, Features, etc.)
│   ├── app/                # Dashboard components (Sidebar, StatCard, etc.)
│   ├── shared/             # Reusable components (Logo, Cards)
│   └── ui/                 # Base UI primitives (Button, Input, Badge)
├── features/               # Feature-specific logic
├── hooks/                  # Custom React hooks
└── lib/                    # Utilities and mock data
```

**Route groups:**

- `(marketing)` - Public routes, no auth required
- `(app)` - Protected app routes

**Mock data:** Development uses mock data from `lib/mock-data.ts` until backend integration is complete.

## Key Conventions

### Backend

**MapStruct Mappers:**

- DTO-to-entity mapping via MapStruct interfaces (`*Mapper.java`)
- Use `@Mapper(componentModel = "spring")` for Spring integration
- Example: `SubscriptionsMapper.toEntity(CreateSubscriptionRequest request)`

**Testing:**

- Integration tests extend `BaseIntegrationTest`
- Use `@SpringBootTest` and `@AutoConfigureTestContainers`
- RestAssured for API testing with `@LocalServerPort`
- Test classes named `*Test.java` (unit) or `*IT.java` (integration)

**Spring Profiles:**

- `dev` - Auto-DDL, debug logging (default for development)
- `production` - SSL required, validate DDL

**Virtual Threads:**

- Enabled via `application.yaml`
- Configured for embedded Tomcat

### Frontend

**Component Organization:**

1. UI primitives (`components/ui/`) - Base components
2. Shared components (`components/shared/`) - Reusable across features
3. Feature components (`components/app/`, `components/sections/`) - Feature-specific

**Styling:**

- Tailwind CSS with custom design system
- Use `clsx` and `tailwind-merge` for conditional classes
- Custom colors and spacing defined in `tailwind.config.ts`

**TypeScript:**

- Strict mode enabled
- All components typed with proper interfaces

## Environment Variables

### Backend (`backend/.env`)

Copy from `backend/.envTemplate`:

```env
ACTIVE_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=local_db
DB_USER=local_user
DB_PASSWORD=local_password
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://ongoing.up.railway.app
```

### Frontend (`frontend/.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1
```

## API

- **Base URL:** `http://localhost:6969/api/v1/`
- **Port:** 6969 (backend), 3000 (frontend)

## Additional Documentation

For detailed explanations and lessons learned, see `docs/FOR-Guilherme.md` which contains:

- Technical architecture deep-dives
- Design decisions and rationale
- Bugs encountered and fixes
- Best practices and pitfalls to avoid
