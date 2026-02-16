# Frontend JWT Authentication Integration Plan

## Context

**Why this change?**
The backend has a complete JWT authentication system with access tokens (short-lived) and refresh tokens (long-lived,
with rotation). The frontend currently uses mock data with no real authentication. We need to integrate the two to
enable user registration, login, session management, and protected routes.

**What prompted it?**
Starting the frontend-backend integration, beginning with the authentication layer as the foundation for all future API
integrations.

**Intended outcome?**
A secure, production-ready authentication system that:

- Allows users to register, log in, and log out
- Maintains sessions across page refreshes (silent refresh)
- Automatically handles token expiration and renewal
- Protects authenticated routes
- Is architected for future migration to HttpOnly cookies

---

## Phased Approach

### Phase 1: Auth Service Layer (Implement Now)

Core authentication infrastructure without UI:

- TypeScript types/interfaces
- Axios HTTP client with interceptors (401 handling, token injection)
- Auth service (login, register, refresh, logout API calls)
- AuthContext + useAuth hook (global state management)
- Token storage (access token in memory, refresh token in localStorage)
- Silent refresh on app load

### Phase 2: UI Components (Future)

- Login page with form validation
- Register page with form validation
- Error handling UI (toast/modal)
- Loading states
- Logout functionality in AppHeader

### Phase 3: Route Protection (Future)

- Next.js middleware for server-side protection
- ProtectedRoute wrapper component
- Redirect logic for unauthenticated users
- "Return to original destination" after login

---

## Architecture Decisions

**Token Storage Strategy (Hybrid):**

- Access token → React state (memory) - lost on page refresh, secure from XSS
- Refresh token → localStorage - persistent across refreshes, enables silent refresh
- **Future**: Migrate to HttpOnly cookies when backend is updated

**Silent Refresh Implementation:**

- On page load: Check localStorage for refresh token, attempt to restore session
- On 401 response: Automatically refresh token and retry failed request
- Request queuing: Multiple simultaneous 401s trigger only one refresh

**HTTP Client Pattern:**

- Axios with request interceptor (inject access token)
- Response interceptor (catch 401, trigger refresh, retry request)
- Callback pattern to avoid circular dependency (api-client ↔ AuthContext)

**Route Protection (Hybrid):**

- Client-side: useAuth() hook checks user state in components
- Server-side (future): Next.js middleware checks cookies
- Works now with localStorage, seamless migration to cookies later

---

## File Structure

```
frontend/src/
├── features/auth/                    # NEW: Dedicated auth module
│   ├── types/auth.types.ts           # TypeScript interfaces (User, AuthResponse, etc.)
│   ├── utils/token-storage.ts        # localStorage abstraction (DELETE when migrating to cookies)
│   ├── services/
│   │   ├── api-client.ts             # Axios instance with interceptors
│   │   └── auth.service.ts           # Auth API methods (login, register, refresh, logout)
│   ├── context/AuthContext.tsx       # React Context + Provider
│   ├── hooks/useAuth.ts              # Custom hook to consume AuthContext
│   └── index.ts                      # Barrel export
│
├── lib/
│   ├── constants.ts                  # NEW: API endpoints, routes
│   ├── utils.ts                      # Existing: cn() utility
│   └── mock-data.ts                  # Existing
│
├── app/
│   └── layout.tsx                    # UPDATED: Wrap with <AuthProvider>
│
└── .env.local                        # NEW: NEXT_PUBLIC_API_URL
```

---

## Key Implementation Details

### 1. TypeScript Types (`features/auth/types/auth.types.ts`)

Defines all interfaces:

- `User` - { id, name, email, role }
- `AuthResponse` - { accessToken, refreshToken } (matches backend)
- `LoginRequest`, `RegisterRequest`, `RefreshRequest`
- `AuthState` - { user, accessToken, isLoading, error, isInitialized }
- `AuthContextValue` - State + methods (login, register, logout, refreshAuth)
- `JwtPayload` - Decoded JWT structure

### 2. API Client (`features/auth/services/api-client.ts`)

**Axios configuration:**

- Base URL: `process.env.NEXT_PUBLIC_API_URL` (http://localhost:6969/api/v1)
- Timeout: 10 seconds
- Content-Type: application/json

**Request interceptor:**

- Injects `Authorization: Bearer {accessToken}` header
- Skips injection for `/auth/login`, `/auth/register`, `/auth/refresh`
- Gets access token via callback (avoids circular dependency)

**Response interceptor:**

- Detects 401 Unauthorized responses
- Triggers `refreshAuth()` callback
- Queues concurrent requests during refresh
- Retries failed requests with new token
- Logs out user if refresh fails

**Callback registration:**

```typescript
registerAuthCallbacks({
    getAccessToken: () => string | null,
    refreshToken: () => Promise<boolean>,
    logout: () => void
})
```

### 3. Auth Service (`features/auth/services/auth.service.ts`)

API methods:

- `register(name, email, password)` → POST /api/v1/auth/register
- `login(email, password)` → POST /api/v1/auth/login
- `refresh(refreshToken)` → POST /api/v1/auth/refresh
- `logout()` → No backend call (tokens are one-time use, expire naturally)
- `decodeToken(token)` → Extract JWT payload (no signature validation)
- `getUserFromToken(accessToken)` → Extract User object from JWT

### 4. Auth Context (`features/auth/context/AuthContext.tsx`)

**State:**

```typescript
{
    user: User | null,              // Current user (null if logged out)
        accessToken
:
    string | null,     // JWT in memory (lost on refresh)
        isLoading
:
    boolean,             // Loading state for auth operations
        error
:
    string | null,           // Last error message
        isInitialized
:
    boolean          // Silent refresh completed?
}
```

**Methods:**

- `login(email, password)` → Call auth service, update state, save tokens
- `register(name, email, password)` → Call auth service, update state, save tokens
- `logout()` → Clear tokens, reset state
- `refreshAuth()` → Get refresh token from localStorage, call /refresh, update state

**Silent refresh on mount:**

```typescript
useEffect(() => {
    const refreshToken = tokenStorage.getRefreshToken()
    if (refreshToken) {
        silentRefresh() // Restore session
    }
    setIsInitialized(true)
}, [])
```

**Callback registration:**

```typescript
useEffect(() => {
    registerAuthCallbacks({
        getAccessToken: () => state.accessToken,
        refreshToken: refreshAuth,
        logout: logout
    })
}, [state.accessToken])
```

### 5. useAuth Hook (`features/auth/hooks/useAuth.ts`)

Simple hook pattern (matches existing `useSidebar()`):

```typescript
export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error("useAuth must be used within AuthProvider")
    }
    return context
}
```

### 6. Token Storage (`features/auth/utils/token-storage.ts`)

localStorage abstraction:

- `getRefreshToken()` → Read from localStorage
- `setRefreshToken(token)` → Write to localStorage
- `removeRefreshToken()` → Clear from localStorage
- `hasRefreshToken()` → Check if exists
- SSR guards: `typeof window === "undefined"`

**Future**: Delete this file when migrating to HttpOnly cookies.

### 7. Root Layout Integration (`app/layout.tsx`)

Wrap app with AuthProvider:

```typescript
export default function RootLayout({children}) {
    return (
        <html>
            <body>
                <AuthProvider>
                    {children}
        < /AuthProvider>
        < /body>
        < /html>
    )
}
```

---

## Data Flow

### Login Flow

```
User enters credentials
  ↓
Component calls login(email, password) from useAuth()
  ↓
AuthContext → authService.login() → POST /api/v1/auth/login
  ↓
Backend returns { accessToken, refreshToken }
  ↓
1. Decode JWT → extract User (id, email, role)
2. Save refreshToken to localStorage
3. Save accessToken to React state (memory)
4. Update context: user, accessToken
  ↓
App re-renders, user is authenticated
```

### Authenticated API Request

```
Component makes API call (e.g., fetch subscriptions)
  ↓
apiClient.get("/subscriptions")
  ↓
Request interceptor:
  - Get access token from callback
  - Add header: Authorization: Bearer {token}
  ↓
Backend validates JWT, returns data
  ↓
Response interceptor: Pass through (200)
  ↓
Component receives data
```

### Token Refresh on 401

```
API request returns 401 Unauthorized
  ↓
Response interceptor detects 401
  ↓
Is refresh already running?
  ├─ Yes → Queue this request
  └─ No → Start refresh process
      ↓
      Get refresh token from localStorage
      ↓
      Call refreshAuth() → POST /api/v1/auth/refresh
      ↓
      Backend returns new { accessToken, refreshToken }
      ↓
      Update tokens in state
      ↓
      Process request queue with new token
      ↓
      Retry original request
```

### Silent Refresh on Page Load

```
App loads, AuthProvider mounts
  ↓
useEffect checks localStorage for refreshToken
  ↓
Found? → Call refreshAuth()
  ↓
POST /api/v1/auth/refresh
  ↓
Success → User logged in
Failure → User logged out
  ↓
Set isInitialized = true
```

---

## Critical Files to Create/Modify

### Phase 1 Implementation Order

1. **`/frontend/src/lib/constants.ts`** (NEW)
    - API_BASE_URL, API_ENDPOINTS, STORAGE_KEYS, ROUTES
    - No dependencies

2. **`/frontend/src/features/auth/types/auth.types.ts`** (NEW)
    - All TypeScript interfaces and types
    - No dependencies

3. **`/frontend/src/features/auth/utils/token-storage.ts`** (NEW)
    - localStorage abstraction with SSR guards
    - Depends on: types

4. **`/frontend/src/features/auth/services/api-client.ts`** (NEW)
    - Axios instance with interceptors
    - Callback registration for auth operations
    - Depends on: types, constants

5. **`/frontend/src/features/auth/services/auth.service.ts`** (NEW)
    - login, register, refresh, logout methods
    - JWT decoding utility
    - Depends on: types, constants, api-client

6. **`/frontend/src/features/auth/context/AuthContext.tsx`** (NEW)
    - Global auth state management
    - Silent refresh logic
    - Callback registration
    - Depends on: types, auth.service, token-storage, api-client

7. **`/frontend/src/features/auth/hooks/useAuth.ts`** (NEW)
    - Custom hook to consume AuthContext
    - Depends on: AuthContext, types

8. **`/frontend/src/features/auth/index.ts`** (NEW)
    - Barrel export for clean imports

9. **`/frontend/src/app/layout.tsx`** (MODIFY)
    - Import AuthProvider
    - Wrap children with `<AuthProvider>`

10. **`/frontend/.env.local`** (NEW)
    - Add: `NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1`

11. **Install axios:**
    ```bash
    cd /home/guilherme/workspace/ongoing/frontend
    npm install axios
    ```

---

## Migration Path to HttpOnly Cookies

When backend sets HttpOnly cookies instead of returning tokens in response body:

**Files to DELETE:**

- `features/auth/utils/token-storage.ts`

**Files to MODIFY:**

- `auth.types.ts`: Remove `refreshToken` from `AuthResponse`, remove `RefreshRequest`
- `auth.service.ts`: Update `refresh()` to not send token in body, update `logout()` to call `/auth/logout` endpoint
- `AuthContext.tsx`: Remove all `tokenStorage` calls, simplify silent refresh
- `api-client.ts`: Add `withCredentials: true`, update refresh interceptor

**Benefits after migration:**

- Refresh tokens immune to XSS (JavaScript cannot access HttpOnly cookies)
- Simpler code (no manual token storage)
- Browser handles cookie lifecycle automatically

---

## Edge Cases & Error Handling

**Token Expiration:**

- Access token expires mid-session → Next API call triggers silent refresh automatically
- Refresh token expires → Silent refresh fails, user logged out, redirect to /login
- Multiple simultaneous 401s → Request queue prevents duplicate refreshes

**Network Errors:**

- Network offline during login → Timeout after 10s, display error message
- Backend down → Display "Unable to connect to server"
- Refresh fails during token rotation → User logged out

**Invalid Data:**

- Invalid email format → Backend returns 400, display validation error
- Password too short → Backend returns 400, display error
- Email already exists → Backend returns 400, display "Email already in use"
- Wrong password → Backend returns 401, display "Invalid credentials"

**Browser Storage:**

- localStorage disabled → App works but can't persist sessions (user must log in after refresh)
- localStorage cleared manually → Silent refresh fails on mount, user must log in
- SSR → Guards prevent localStorage access on server

**Race Conditions:**

- Multiple tabs open, one logs out → Other tabs log out on next API call (refresh token gone)
- Page refresh during login → Silent refresh on mount restores session
- Refresh triggered while another refresh running → Request queue prevents duplicates

---

## Testing Strategy

**Unit Tests (Phase 1):**

- `auth.service.test.ts`: API calls, JWT decoding, user extraction
- `api-client.test.ts`: Interceptors, 401 handling, request queuing
- `token-storage.test.ts`: localStorage operations, SSR guards
- `useAuth.test.tsx`: Hook error handling, context consumption

**Integration Tests (Phase 2):**

- `AuthContext.integration.test.tsx`: Login flow, silent refresh, logout
- `LoginForm.integration.test.tsx`: Form validation, error display, redirect
- `RegisterForm.integration.test.tsx`: Registration flow, validation

**Manual Testing Checklist (Phase 1):**

- [ ] Open app → silent refresh runs on mount
- [ ] Login with valid credentials → tokens saved, user logged in
- [ ] Login with invalid credentials → error displayed
- [ ] Make API call → Authorization header present
- [ ] Wait for token expiry → next API call triggers refresh
- [ ] Logout → tokens cleared, user logged out
- [ ] Open in incognito → works without localStorage
- [ ] Multiple tabs → logout in one, other tabs log out on next action

---

## Verification Steps (End-to-End)

After implementing Phase 1, verify the system works:

1. **Start backend and frontend:**
   ```bash
   # Backend
   cd backend && ./mvnw spring-boot:run

   # Frontend
   cd frontend && npm run dev
   ```

2. **Open browser DevTools → Console**

3. **Navigate to http://localhost:3000**
    - Check console: Silent refresh attempt (should fail if no token)
    - Verify no errors

4. **Test via Console (temporary, until UI is built):**
   ```javascript
   // Get auth context (exposed globally for testing)
   const { login } = window.__auth

   // Register a user
   await window.__auth.register("Test User", "test@example.com", "password123")
   // → Check: User logged in, tokens in localStorage

   // Logout
   window.__auth.logout()
   // → Check: Tokens cleared

   // Login
   await window.__auth.login("test@example.com", "password123")
   // → Check: User logged in

   // Make authenticated API call
   const { apiClient } = await import("/src/features/auth/services/api-client.ts")
   await apiClient.get("/subscriptions")
   // → Check: Authorization header present, request succeeds
   ```

5. **Verify localStorage:**
    - DevTools → Application → Local Storage
    - Check: `ongoing_refresh_token` present after login
    - Check: Token removed after logout

6. **Test token refresh:**
    - Backend config: Set JWT expiration to 10 seconds (for testing)
    - Login, wait 15 seconds, make API call
    - Check console: Silent refresh triggered, request succeeded

7. **Test error handling:**
    - Try login with wrong password → Check error displayed
    - Turn off backend, try login → Check network error displayed
    - Turn on backend, verify recovery

---

## Future Phases Summary

### Phase 2: UI Components

**Scope:** Login and Register pages with forms

**Files to create:**

- `app/(auth)/layout.tsx` - Centered layout for auth pages
- `app/(auth)/login/page.tsx` - Login page
- `app/(auth)/register/page.tsx` - Register page
- `components/auth/LoginForm.tsx` - Login form with validation
- `components/auth/RegisterForm.tsx` - Register form with validation

**Files to modify:**

- `components/layout/Header.tsx` - Dynamic "Entrar"/"Dashboard" button based on auth state
- `components/app/AppHeader.tsx` - Add logout button in user dropdown

**Features:**

- Client-side form validation
- Loading states during submission
- Error display from useAuth()
- Redirect to /dashboard after successful login/register
- Link between login and register pages

### Phase 3: Route Protection

**Scope:** Middleware and client-side protection

**Files to create:**

- `middleware.ts` - Next.js middleware for server-side route protection
- `components/auth/ProtectedRoute.tsx` - Client-side wrapper component

**Files to modify:**

- `app/(app)/layout.tsx` - Wrap with ProtectedRoute component

**Features:**

- Server-side route protection via middleware (reads cookies in future)
- Client-side protection via useAuth() check
- Loading state while isInitialized is false
- Redirect to /login if not authenticated
- "Return to origin" after login (using ?redirect query param)
- Protect routes: /dashboard, /subscriptions, /categories, /calendar, /settings

---

## Dependencies

**New dependencies to install:**

- `axios` (HTTP client with interceptors)

**Existing dependencies (already installed):**

- `next` 14.2.35
- `react` ^18
- `typescript` ^5
- `clsx` ^2.1.1
- `tailwind-merge` ^3.4.0

---

## Notes

**Security Considerations:**

- Access token in memory: Secure from XSS, lost on page refresh (intended)
- Refresh token in localStorage: Vulnerable to XSS, mitigated by short access token lifetime
- Future HttpOnly cookies: Eliminates XSS risk entirely

**Backend Integration:**

- No backend changes required for Phase 1
- Backend already implements token rotation (refresh tokens are one-time use)
- Backend DOES NOT have logout endpoint (not needed with token rotation)

**Coding Patterns to Follow:**

- Follow existing SidebarContext pattern for AuthContext
- Use `cn()` utility for className merging
- Use forwardRef for reusable components
- Mark client components with `"use client"` directive
- Use path alias `@/*` for imports

**Common Pitfalls to Avoid:**

- Circular dependency between api-client and AuthContext → Use callback pattern
- Accessing localStorage during SSR → Add `typeof window === "undefined"` guards
- Multiple simultaneous refreshes → Implement request queue
- Infinite refresh loop → Add `_retry` flag to requests
- Token injection to auth endpoints → Skip Authorization header for /auth/* routes
