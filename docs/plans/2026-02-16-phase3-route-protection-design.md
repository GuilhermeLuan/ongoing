# Phase 3: Route Protection - Design Document

## Context

**Base plan:** `docs/plans/2026-02-15-frontend-jwt-integration-plan.md` (Phase 3 section)

**What we're building:** Client-side route protection that prevents unauthenticated users from accessing
protected pages, redirects authenticated users away from login/register, and preserves the user's intended
destination through login.

**Design decisions made:**

- Client-side only (no Next.js middleware) — middleware can't read localStorage tokens
- Redirect with "return to origin" via `?redirect` query param
- Minimal loading state (logo + spinner) while auth initializes
- Authenticated users redirected away from /login and /register
- Middleware deferred to future HttpOnly cookie migration

---

## Security Rationale

### Why client-side only?

The original plan proposed hybrid protection (client-side + middleware). However, since tokens live in
localStorage (not cookies), Next.js middleware has **no access** to authentication state — it runs on the
edge and can only read cookies.

Workarounds (auxiliary cookies, duplicated tokens) were evaluated and rejected:

| Approach                              | Problem                                                              |
|---------------------------------------|----------------------------------------------------------------------|
| Auxiliary cookie (`has_session=true`) | Non-HttpOnly, spoofable via `document.cookie`, no real security gain |
| Duplicated refresh token in cookie    | Increases attack surface — token now stealable from two places       |

### Why this is secure enough

Route protection is a **UX concern**, not a security boundary. The real security lives in the backend:

- Every API call requires a valid JWT — without it, the backend returns 401
- Even if someone reaches `/dashboard`, they see no data without a valid token
- The current localStorage + Bearer header setup is **immune to CSRF** (browser never sends Authorization headers
  automatically)
- XSS risk is mitigated by React (escapes output by default) and backend token rotation (one-time-use refresh tokens)

**When to add middleware:** After migrating to HttpOnly cookies, the middleware can validate real tokens
that JavaScript cannot access — eliminating both XSS and CSRF vectors simultaneously.

---

## File Structure

### Files to Create

```
frontend/src/components/auth/
├── ProtectedRoute.tsx    # Guards authenticated routes (app)/*
├── GuestRoute.tsx        # Guards auth routes (auth)/*
└── index.ts              # Update barrel export (add new components)
```

### Files to Modify

```
frontend/src/
├── app/(app)/layout.tsx              # Wrap children with <ProtectedRoute>
├── app/(auth)/layout.tsx             # Wrap children with <GuestRoute>
├── components/auth/LoginForm.tsx     # Read ?redirect, use as post-login destination
├── components/auth/RegisterForm.tsx  # Read ?redirect, use as post-register destination
```

---

## Component Specifications

### ProtectedRoute

**Location:** `components/auth/ProtectedRoute.tsx`

**Purpose:** Wraps all `(app)/*` routes. Ensures only authenticated users see protected content.

**Behavior:**

```
Component mounts
  ↓
isInitialized === false?
  → Render loading screen (logo + spinner, full screen)
  ↓
isInitialized === true, user === null?
  → useEffect: router.replace("/login?redirect={currentPath}")
  → Render loading screen (while redirect happens)
  ↓
isInitialized === true, user exists?
  → Render {children}
```

**Implementation details:**

- `"use client"` directive
- Uses `useAuth()` to read `isInitialized` and `user`
- Uses `usePathname()` from `next/navigation` to capture current route for redirect param
- Uses `router.replace()` (not `push`) — keeps `/login` out of browser history, so "back" button
  doesn't return to a protected route
- Redirect runs inside `useEffect`, not during render
- Props: `{ children: React.ReactNode }`

### GuestRoute

**Location:** `components/auth/GuestRoute.tsx`

**Purpose:** Wraps all `(auth)/*` routes. Redirects authenticated users away from login/register.

**Behavior:**

```
Component mounts
  ↓
isInitialized === false?
  → Render loading screen (logo + spinner, full screen)
  ↓
isInitialized === true, user exists?
  → useEffect: router.replace("/dashboard")
  → Render loading screen (while redirect happens)
  ↓
isInitialized === true, user === null?
  → Render {children}
```

**Implementation details:**

- `"use client"` directive
- Uses `useAuth()` to read `isInitialized` and `user`
- Uses `router.replace("/dashboard")` — same history reasoning as ProtectedRoute
- Redirect runs inside `useEffect`, not during render
- Props: `{ children: React.ReactNode }`

### Shared Loading Screen

**Used by both ProtectedRoute and GuestRoute.**

**Visual design:**

- Full screen: `min-h-screen`
- Background: `bg-neutral-50` (consistent with app)
- Centered: `flex items-center justify-center`
- Logo component (existing `<Logo />`) at center
- Spinner below logo: CSS ring with `animate-spin` (`border-4 border-primary-200 border-t-primary-600 rounded-full`)
- No text — clean and fast

**Implementation:** Inline JSX in each component (no separate component needed — it's ~5 lines of JSX).

---

## Modifications to Existing Files

### app/(app)/layout.tsx

**Change:** Wrap children with `<ProtectedRoute>`.

```tsx
import { ProtectedRoute } from "@/components/auth"

export default function AppLayout({ children }) {
  return (
    <ProtectedRoute>
      <SidebarProvider>
        <Sidebar />
        <div className="lg:pl-60 min-h-screen bg-neutral-50">
          {children}
        </div>
      </SidebarProvider>
    </ProtectedRoute>
  )
}
```

**Note:** `ProtectedRoute` wraps **outside** `SidebarProvider` — if user is not authenticated, we don't
render sidebar at all.

### app/(auth)/layout.tsx

**Change:** Wrap children with `<GuestRoute>`.

```tsx
import { GuestRoute } from "@/components/auth"

export default function AuthLayout({ children }) {
  return (
    <GuestRoute>
      {/* existing split-screen layout */}
    </GuestRoute>
  )
}
```

### LoginForm.tsx

**Changes:**

1. Import `useSearchParams` from `next/navigation`
2. Read `redirect` param: `const redirect = searchParams.get("redirect")`
3. Validate redirect starts with `/` (prevents open redirect to external URLs)
4. Use as destination on success: `router.push(redirect || "/dashboard")`

```tsx
const searchParams = useSearchParams()

// On successful login:
const redirect = searchParams.get("redirect")
const destination = redirect && redirect.startsWith("/") ? redirect : "/dashboard"
router.push(destination)
```

### RegisterForm.tsx

**Same changes as LoginForm.tsx** — read `?redirect` and use as post-register destination.

---

## Data Flows

### Unauthenticated user visits protected route

```
User navigates to /subscriptions
  ↓
(app)/layout.tsx → <ProtectedRoute>
  ↓
isInitialized === false → Loading screen (logo + spinner)
  ↓
Silent refresh runs (AuthProvider)
  ↓
No refresh token found → isInitialized = true, user = null
  ↓
ProtectedRoute useEffect → router.replace("/login?redirect=/subscriptions")
  ↓
User sees login page
  ↓
User logs in successfully
  ↓
LoginForm reads ?redirect=/subscriptions
  ↓
router.push("/subscriptions") → User arrives at intended destination
```

### Authenticated user visits /login

```
User navigates to /login
  ↓
(auth)/layout.tsx → <GuestRoute>
  ↓
isInitialized === false → Loading screen
  ↓
Silent refresh runs → Success → user exists
  ↓
GuestRoute useEffect → router.replace("/dashboard")
  ↓
User lands on dashboard
```

### Fresh visit (no session)

```
User navigates to /dashboard
  ↓
<ProtectedRoute> → isInitialized = false → Loading screen
  ↓
Silent refresh → No refresh token → Fails
  ↓
isInitialized = true, user = null
  ↓
router.replace("/login?redirect=/dashboard")
```

---

## Protected vs Public Routes

| Route            | Group         | Protection                              |
|------------------|---------------|-----------------------------------------|
| `/`              | `(marketing)` | Public                                  |
| `/login`         | `(auth)`      | GuestRoute (redirects if authenticated) |
| `/register`      | `(auth)`      | GuestRoute (redirects if authenticated) |
| `/dashboard`     | `(app)`       | ProtectedRoute                          |
| `/subscriptions` | `(app)`       | ProtectedRoute                          |
| `/categories`    | `(app)`       | ProtectedRoute                          |
| `/calendar`      | `(app)`       | ProtectedRoute                          |
| `/settings`      | `(app)`       | ProtectedRoute                          |

---

## Edge Cases

**Silent refresh in progress:** Both components show loading until `isInitialized = true`. No flash of
wrong content.

**Direct URL access:** Works correctly — ProtectedRoute captures the pathname and passes it as `?redirect`.

**Browser back button after redirect:** Using `router.replace()` instead of `push` means the protected
route is not in the history stack. Back button goes to the page before, not to a protected route that
would redirect again.

**Open redirect prevention:** LoginForm/RegisterForm validate that `?redirect` starts with `/`. Values
like `https://evil.com` are ignored, falling back to `/dashboard`.

**Multiple tabs:** If user logs out in one tab, other tabs will fail on next API call (401 → refresh
fails → logout). ProtectedRoute will then redirect to `/login` on next render.

---

## Implementation Order

1. `ProtectedRoute.tsx` — Create component
2. `GuestRoute.tsx` — Create component
3. `components/auth/index.ts` — Update barrel export
4. `app/(app)/layout.tsx` — Wrap with ProtectedRoute
5. `app/(auth)/layout.tsx` — Wrap with GuestRoute
6. `LoginForm.tsx` — Add ?redirect handling
7. `RegisterForm.tsx` — Add ?redirect handling

---

## What This Does NOT Include

- **Next.js middleware** — Deferred to HttpOnly cookie migration
- **Role-based access control** — All authenticated users have same access for now
- **Session timeout UI** — No "your session expired" modal (user just gets redirected to login)
- **Remember me** — Refresh token persistence already handles this via localStorage
