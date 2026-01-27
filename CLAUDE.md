# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4 REST API for tracking recurring subscriptions. Built with Java 25, Spring Data JPA, PostgreSQL, and Flyway
for database migrations.

## Build and Run Commands

```bash
# Build the project
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Run the application (dev profile)
./mvnw spring-boot:run

# Start PostgreSQL via Docker Compose
docker-compose up -d
```

## Testing Commands

```bash
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

## Architecture

**Layered architecture** with package-by-feature organization:

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

## Key Patterns

- **MapStruct** for DTO-to-entity mapping (interfaces in `*Mapper.java`)
- **Flyway** migrations in `src/main/resources/db/migration/`
- **Testcontainers** for integration tests - extend `BaseIntegrationTest`
- **Virtual threads** enabled via application.yaml
- **Profiles**: `dev` (auto-DDL, debug logging), `production` (SSL, validate DDL)

## API Base

Server runs on port `6969`. All endpoints prefixed with `/api/v1/`.

## Environment Setup

Copy `.envTemplate` to `.env` and configure PostgreSQL credentials. The `ACTIVE_PROFILE` variable controls which Spring
profile is used.

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