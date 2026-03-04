# Cache Strategy: Subscriptions + Dashboard (Hybrid)

## Context

Today, `subscriptions` and `dashboard` are fetched directly on each request. The backend already has Redis and Spring Cache enabled, but caching is currently focused on exchange rates. On the frontend, hooks fetch on mount/refetch without a dedicated cache layer.

## Decision

Adopt a **hybrid cache strategy**:

1. **Backend cache (Redis)** as the source of truth for shared performance gains.
2. **Frontend in-memory cache** with short TTL for faster navigation UX.

This balances scalability (server-side) and responsiveness (client-side).

## Scope

- Backend:
  - `GET /api/v1/subscriptions` (list with filters/pagination)
  - `GET /api/v1/subscriptions/{id}` (detail)
  - `GET /api/v1/dashboard`
- Frontend:
  - `useSubscriptions`
  - `useDashboard`

## Backend Plan

### 1) Cache reads with user-scoped keys

- Include `userId` in all keys.
- For subscriptions list, include relevant query params (`page`, `size`, `sort`, `name`, `active`, `categoryId`).

Suggested cache names:
- `subscriptions-list`
- `subscriptions-by-id`
- `dashboard`

### 2) TTL policy (initial)

- `dashboard`: 30-60 seconds
- `subscriptions-list`: 1-3 minutes
- `subscriptions-by-id`: 1-3 minutes

Use short TTLs first, then tune with usage telemetry.

### 3) Invalidation policy

On create/update/delete subscription:
- Evict `subscriptions-list` for the user
- Evict `subscriptions-by-id` for the affected id/user
- Evict `dashboard` for the user

Invalidation must happen in mutation flow to avoid stale totals.

## Frontend Plan

### 1) Memory cache in hooks

- Add lightweight cache map with timestamp + data.
- Cache key by query params for subscriptions and fixed key per user/session for dashboard.

### 2) Freshness model

- Return cached data immediately when valid.
- Trigger background refresh (stale-while-revalidate) where appropriate.
- Keep TTL short to reduce staleness risk.

### 3) Frontend invalidation

After create/update/delete subscription:
- Invalidate subscriptions cache entries
- Invalidate dashboard cache entry
- Refetch visible data

On logout:
- Clear auth and cache memory.

## Rollout Phases

1. Backend read caching.
2. Backend invalidation on mutations.
3. Frontend cache layer in hooks.
4. Frontend invalidation + refetch integration.
5. Tests and monitoring.

## Validation

- Backend tests:
  - cache hit/miss behavior
  - user isolation in keys
  - mutation invalidation correctness
- Frontend tests:
  - TTL expiration behavior
  - stale-while-revalidate behavior
  - invalidation after mutation/logout

## Risks and Mitigations

- **Stale data**: keep short TTL + strict invalidation.
- **Cross-user leakage**: enforce `userId` in all keys.
- **Key explosion**: limit cached dimensions to effective query params only.

