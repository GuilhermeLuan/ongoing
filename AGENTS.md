# Repository Guidelines

## Project Structure & Module Organization

This monorepo contains a Spring Boot backend and a Next.js frontend. Backend code lives in `backend/src/main/java/dev/guilhermeluan/ongoing/`, organized by feature packages such as `subscriptions`, `auth`, `dashboard`, `notification`, and `status`. Backend tests are in `backend/src/test/java/`; unit tests use `*Test.java` and integration tests use `*IT.java`. Migrations are in `backend/src/main/resources/db/migration/`.

Frontend code lives in `frontend/src/`. Routes use the Next.js App Router under `src/app/`, with `(marketing)` for public pages and `(app)` for authenticated screens. Reusable UI is under `src/components/ui`, app components under `src/components/app`, and feature logic under `src/features`. Docs and plans are in `docs/`.

## Build, Test, and Development Commands

Run commands from the relevant subdirectory.

```bash
docker-compose up -d              # Start local PostgreSQL
cd backend && ./mvnw spring-boot:run
cd backend && ./mvnw test          # Backend unit tests
cd backend && ./mvnw verify        # Backend unit + integration tests
cd frontend && npm ci              # Install frontend dependencies
cd frontend && npm run dev         # Start Next.js at localhost:3000
cd frontend && npm run lint        # ESLint
cd frontend && npm run build       # Production build
cd frontend && npm run test        # Vitest
```

## Coding Style & Naming Conventions

Backend uses Java 25, Spring Boot, Lombok, and MapStruct. Keep the package-by-feature layout, name controllers/services/repositories with their Spring roles, and use records for DTOs where practical. Flyway files should follow `V#__description.sql`.

Frontend uses TypeScript, React, Tailwind CSS, and the `@/*` path alias. Prefer barrel imports from component folders, for example `import { Button } from "@/components/ui"`. Use PascalCase for components, camelCase for hooks/utilities, and `use*` for React hooks.

## Testing Guidelines

Backend integration tests should extend `backend/src/test/java/dev/guilhermeluan/ongoing/config/BaseIntegrationTest.java` when database or API behavior is involved. Use RestAssured for controller integration tests. Frontend tests use Vitest and Testing Library; place tests next to code as `*.test.ts` or `*.test.tsx`.

## Commit & Pull Request Guidelines

Git history follows concise Conventional Commit-style messages such as `feat: remove mocked sidebar menus`, `fix(auth): prevent refresh flow on 401 from /auth/refresh`, and `docs(plan): add hybrid cache strategy`. Keep commits focused and scoped.

Pull requests should include a short description, linked issue when applicable, testing performed, and screenshots or recordings for UI changes. Ensure backend tests, frontend lint, and frontend build pass.

## Security & Configuration Tips

Do not commit secrets. Copy `backend/.envTemplate` to `backend/.env` for local backend settings, and create `frontend/.env.local` with `NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1` for local frontend API access.
