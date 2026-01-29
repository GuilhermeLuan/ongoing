# Subscription Filters Design

## Context

The `GET /api/v1/subscriptions` endpoint currently returns all subscriptions with basic pagination. There is no way to
filter results by name, status, currency, or category.

## Decision

Implement filtering using a single JPQL query with optional parameters in the repository. This approach was chosen over
Specifications (more boilerplate than needed for 4 filters) and QueryDSL (heavy setup for a simple use case).

## API

```
GET /api/v1/subscriptions?name=netflix&active=true&currency=BRL&categoryId=1&page=0&size=20&sort=name,asc
```

All filter parameters are optional. When omitted, the filter is not applied. Multiple filters combine with AND logic.

| Parameter    | Type     | Match behavior                   |
|--------------|----------|----------------------------------|
| `name`       | String   | Partial, case-insensitive (LIKE) |
| `active`     | Boolean  | Exact match                      |
| `currency`   | Currency | Exact match (BRL, USD, EUR)      |
| `categoryId` | Long     | Exact match on category FK       |

## Implementation

### Repository

Add a `findWithFilters` method with `@Query` using the `:param IS NULL OR condition` pattern:

```java

@Query("""
        SELECT s FROM Subscriptions s
        WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:active IS NULL OR s.active = :active)
          AND (:currency IS NULL OR s.currency = :currency)
          AND (:categoryId IS NULL OR s.category.id = :categoryId)
        """)
Page<Subscriptions> findWithFilters(
        @Param("name") String name,
        @Param("active") Boolean active,
        @Param("currency") Currency currency,
        @Param("categoryId") Long categoryId,
        Pageable pageable);
```

### Service

Update `findAll` to accept filter parameters and delegate to `findWithFilters`.

### Controller

Add `@RequestParam(required = false)` for each filter parameter on the existing `GET /` endpoint.

### Tests

Add integration test scenarios for:

- Filter by name (partial match)
- Filter by active status
- Filter by currency
- Filter by categoryId
- Combined filters
- No filters (existing behavior preserved)

## Files to change

- `SubscriptionsRepository.java` — add `findWithFilters` method
- `SubscriptionsService.java` — update `findAll` signature
- `SubscriptionsController.java` — add `@RequestParam` parameters
- `SubscriptionsControllerIT.java` — add filter test scenarios
