# Ongoing - Subscription Management Platform

A full-stack application for tracking recurring subscriptions, built as a monorepo with Spring Boot backend and Next.js frontend.

## Project Structure

```
ongoing/
├── backend/          # Spring Boot REST API
├── frontend/         # Next.js landing page and dashboard
├── docs/             # Project documentation
└── docker-compose.yaml
```

## Quick Start

### Prerequisites

- Java 25
- Node.js 18+ (for frontend)
- Docker and Docker Compose (for PostgreSQL)
- Maven (included via wrapper)

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:6969/api/v1/`

### 3. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000`

## Backend

Spring Boot 4 REST API with:
- Java 25 with virtual threads
- Spring Data JPA + PostgreSQL
- Flyway migrations
- MapStruct for DTO mapping
- Testcontainers for integration tests

**Port:** 6969
**API Base:** `/api/v1/`

See [backend/HELP.md](backend/HELP.md) for detailed backend documentation.

### Backend Commands

```bash
cd backend

# Build
./mvnw clean package

# Run tests
./mvnw test

# Run integration tests
./mvnw verify
```

## Frontend

Next.js 14 application with:
- TypeScript
- Tailwind CSS
- App Router
- Landing page + Dashboard

**Port:** 3000

### Frontend Commands

```bash
cd frontend

# Development
npm run dev

# Build
npm run build

# Production
npm start

# Lint
npm run lint
```

## Environment Variables

### Backend

Copy `backend/.envTemplate` to `backend/.env` and configure:

```env
ACTIVE_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=local_db
DB_USER=local_user
DB_PASSWORD=local_password
```

### Frontend

Create `frontend/.env.local` for environment-specific variables:

```env
NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1
```

## Documentation

- [Backend Details](backend/HELP.md)
- [Frontend Details](frontend/README.md)
- [Architecture & Design](docs/)

## Development Workflow

1. Start PostgreSQL: `docker-compose up -d`
2. Run backend: `cd backend && ./mvnw spring-boot:run`
3. Run frontend: `cd frontend && npm run dev`
4. Access frontend at http://localhost:3000
5. API available at http://localhost:6969/api/v1/

## Contributing

This is a personal project, but feedback and suggestions are welcome!

## License

Private project - All rights reserved
