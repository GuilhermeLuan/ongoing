# Phase 2: UI Components - Design Document

## Context

**Base plan:** `docs/plans/2026-02-15-frontend-jwt-integration-plan.md` (Phase 2 section)

**What we're building:** Login and Register pages with form validation, error handling, and logout
functionality in the dashboard header. This builds on the Phase 1 auth service layer already implemented.

**Design decisions made:**

- Split screen layout for auth pages (branding panel + form)
- Inline validation with useState (no external libraries)
- Alert inline for backend errors (no toast system)
- Redirect to /dashboard after successful login/register
- Marketing header links stay as-is (sandbox mode in future backlog)

---

## File Structure

### Files to Create

```
frontend/src/
├── app/(auth)/
│   ├── layout.tsx              # Split screen layout (branding + form)
│   ├── login/page.tsx          # Login page (renders LoginForm)
│   └── register/page.tsx       # Register page (renders RegisterForm)
├── components/auth/
│   ├── LoginForm.tsx           # Login form with validation
│   ├── RegisterForm.tsx        # Register form with validation
│   ├── AuthAlert.tsx           # Inline error alert component
│   └── index.ts                # Barrel export
```

### Files to Modify

```
frontend/src/
├── components/app/
│   └── AppHeader.tsx           # Add user dropdown with logout
│   └── index.ts                # Export new components if needed
```

---

## Implementation Details

### 1. Auth Layout (`app/(auth)/layout.tsx`)

**Split screen layout** - completely isolated from marketing and app layouts.

**Desktop (lg+):**

- Left panel (50% width): Gradient background `from-primary-500 to-accent-500`
    - Logo component centered
    - Tagline: "Gerencie suas assinaturas de forma inteligente"
    - Decorative circles with `opacity-10` for visual interest
    - White text throughout
- Right panel (50% width): `bg-neutral-50`
    - Form centered vertically and horizontally
    - Max width constrained (~420px) for readability

**Mobile (< lg):**

- Branding panel becomes compact header (~150px height) with gradient, logo, and tagline
- Form fills remaining screen space below
- Scrollable if content overflows

**Key styling:**

```
Left:  bg-gradient-to-br from-primary-500 to-accent-500, flex items-center justify-center
Right: bg-neutral-50, flex items-center justify-center, p-8
```

No Header, no Footer, no Sidebar.

---

### 2. Login Page (`app/(auth)/login/page.tsx`)

Thin wrapper that renders `<LoginForm />`. Metadata: `title: "Entrar | Ongoing"`.

---

### 3. Login Form (`components/auth/LoginForm.tsx`)

**Directive:** `"use client"`

**State:**

```typescript
const [email, setEmail] = useState("")
const [password, setPassword] = useState("")
const [errors, setErrors] = useState<{ email?: string; password?: string }>({})
const [isSubmitting, setIsSubmitting] = useState(false)
```

**Imports:** `useAuth()` hook, `useRouter()` from next/navigation, `Input`, `Button` from `@/components/ui`,
`AuthAlert` from `@/components/auth`, `Mail`, `Lock` icons from lucide-react.

**Layout (top to bottom):**

1. **Title:** "Entrar na sua conta"
    - `text-2xl font-display font-bold text-neutral-900`
2. **Subtitle:** "Bem-vindo de volta! Entre com seus dados."
    - `text-neutral-500 mt-2`
3. **Spacing:** `mt-8`
4. **Email field:** `<Input label="Email" type="email" icon={<Mail />} error={errors.email} />`
5. **Password field:** `<Input label="Senha" type="password" icon={<Lock />} error={errors.password} />`
6. **Forgot password link:** "Esqueceu a senha?" (placeholder, not functional yet)
    - `text-sm text-primary-500 hover:text-primary-600 text-right mt-1`
    - Renders as `<span>` or disabled link (no href, cursor-default)
7. **AuthAlert:** Conditionally rendered when `useAuth().error` is not null
    - `mt-4`
8. **Submit button:** "Entrar" / "Entrando..." when loading
    - `<Button variant="primary" size="lg" className="w-full mt-6" disabled={isSubmitting}>`
9. **Register link:** "Nao tem uma conta?" + "Registre-se" link to `/register`
    - `text-sm text-neutral-500 text-center mt-6`
    - "Registre-se" as `<Link>` with `text-primary-500 hover:text-primary-600 font-medium`

**Validation (on submit only):**

```typescript
function validate(): boolean {
    const newErrors: typeof errors = {}

    if (!email.trim()) {
        newErrors.email = "Email e obrigatorio"
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        newErrors.email = "Email invalido"
    }

    if (!password) {
        newErrors.password = "Senha e obrigatoria"
    } else if (password.length < 6) {
        newErrors.password = "Senha deve ter no minimo 6 caracteres"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
}
```

**Submit flow:**

```typescript
async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!validate()) return

    setIsSubmitting(true)
    clearError() // from useAuth

    const success = await login(email, password)
    if (success) {
        router.push("/dashboard")
    }

    setIsSubmitting(false)
}
```

**Note:** `login()` from useAuth already sets `error` state on failure, which AuthAlert reads.

---

### 4. Register Page (`app/(auth)/register/page.tsx`)

Thin wrapper that renders `<RegisterForm />`. Metadata: `title: "Registrar | Ongoing"`.

---

### 5. Register Form (`components/auth/RegisterForm.tsx`)

**Directive:** `"use client"`

**State:**

```typescript
const [name, setName] = useState("")
const [email, setEmail] = useState("")
const [password, setPassword] = useState("")
const [confirmPassword, setConfirmPassword] = useState("")
const [errors, setErrors] = useState<{
    name?: string
    email?: string
    password?: string
    confirmPassword?: string
}>({})
const [isSubmitting, setIsSubmitting] = useState(false)
```

**Imports:** Same as LoginForm + `User` icon from lucide-react.

**Layout (top to bottom):**

1. **Title:** "Criar sua conta"
    - `text-2xl font-display font-bold text-neutral-900`
2. **Subtitle:** "Comece a gerenciar suas assinaturas hoje."
    - `text-neutral-500 mt-2`
3. **Spacing:** `mt-8`
4. **Name field:** `<Input label="Nome completo" type="text" icon={<User />} error={errors.name} />`
5. **Email field:** `<Input label="Email" type="email" icon={<Mail />} error={errors.email} />`
6. **Password field:** `<Input label="Senha" type="password" icon={<Lock />} error={errors.password} />`
7. **Confirm password field:** `<Input label="Confirmar senha" type="password" icon={<Lock />}
   error={errors.confirmPassword} />`
8. **AuthAlert:** Conditionally rendered when `useAuth().error` is not null
9. **Submit button:** "Criar conta" / "Criando conta..." when loading
    - `<Button variant="primary" size="lg" className="w-full mt-6" disabled={isSubmitting}>`
10. **Login link:** "Ja tem uma conta?" + "Entrar" link to `/login`
    - Same styling as login form's register link

**Validation (on submit only):**

```typescript
function validate(): boolean {
    const newErrors: typeof errors = {}

    if (!name.trim()) {
        newErrors.name = "Nome e obrigatorio"
    } else if (name.trim().length < 2) {
        newErrors.name = "Nome deve ter no minimo 2 caracteres"
    }

    if (!email.trim()) {
        newErrors.email = "Email e obrigatorio"
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        newErrors.email = "Email invalido"
    }

    if (!password) {
        newErrors.password = "Senha e obrigatoria"
    } else if (password.length < 6) {
        newErrors.password = "Senha deve ter no minimo 6 caracteres"
    }

    if (!confirmPassword) {
        newErrors.confirmPassword = "Confirme sua senha"
    } else if (confirmPassword !== password) {
        newErrors.confirmPassword = "As senhas nao coincidem"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
}
```

**Submit flow:**

```typescript
async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!validate()) return

    setIsSubmitting(true)
    clearError()

    const success = await register(name, email, password)
    if (success) {
        router.push("/dashboard")
    }

    setIsSubmitting(false)
}
```

---

### 6. AuthAlert (`components/auth/AuthAlert.tsx`)

**Reusable inline alert component** following UI component patterns (forwardRef + cn).

**Props:**

```typescript
interface AuthAlertProps {
    message: string
    variant?: "error" | "warning"
    className?: string
}
```

**Styling:**

- Error: `bg-red-50 border border-red-200 text-red-700`
- Warning: `bg-amber-50 border border-amber-200 text-amber-700`
- Base: `rounded-lg px-4 py-3 flex items-center gap-3 text-sm animate-fadeIn`
- Icon: `AlertCircle` from lucide-react (size 18, `flex-shrink-0`)

---

### 7. AppHeader Modification (`components/app/AppHeader.tsx`)

**Current state:** Sticky header with menu button (mobile), title, search, notification bell, avatar.

**Changes:**

Add a user dropdown menu triggered by clicking the avatar area.

**New state:**

```typescript
const [isDropdownOpen, setIsDropdownOpen] = useState(false)
const {user, logout} = useAuth()
const router = useRouter()
```

**Dropdown trigger:** Replace the static avatar with a clickable button that toggles the dropdown.

**Dropdown menu (absolute positioned):**

```
Position: absolute right-0 top-full mt-2
Styling: bg-white rounded-xl shadow-elevated border border-neutral-100 py-2 min-w-[200px] z-50
Animation: animate-fadeIn
```

**Dropdown contents:**

1. **User info section** (px-4 py-3):
    - Name: `text-sm font-medium text-neutral-900` (from `user.name`)
    - Email: `text-xs text-neutral-500` (from `user.email`)
2. **Separator:** `border-t border-neutral-100 my-1`
3. **Logout button** (full width, px-4 py-2):
    - Icon: `LogOut` from lucide-react (size 16)
    - Text: "Sair"
    - Styling: `text-red-600 hover:bg-red-50 flex items-center gap-2 text-sm rounded-lg mx-1`
    - Action: `logout()` then `router.push("/login")`

**Close on outside click:**

```typescript
useEffect(() => {
    if (!isDropdownOpen) return

    function handleClickOutside(e: MouseEvent) {
        if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
            setIsDropdownOpen(false)
        }
    }

    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
}, [isDropdownOpen])
```

**Avatar display:**

- If `user` exists: Show Avatar with `user.name` initials (using existing Avatar component)
- If no user: Show default avatar placeholder

---

## AuthContext Changes Required

The current `AuthContext` needs a small adjustment for Phase 2:

**login() and register() should return a boolean** indicating success/failure, so forms know whether
to redirect. Check if this is already the case in the Phase 1 implementation. If login/register currently
return `void`, update them to return `Promise<boolean>`.

**clearError() method** should exist to clear the error state before a new submission. Check if already
implemented.

---

## Data Flow

### Login Flow

```
User fills form → Submit
  → Client validation (email format, password length)
  → If invalid → Show inline errors below fields
  → If valid → setIsSubmitting(true) → clearError()
    → useAuth().login(email, password)
      → authService.login() → POST /api/v1/auth/login
      → Success → returns true → router.push("/dashboard")
      → Failure → sets error in AuthContext → returns false
        → AuthAlert renders error message
    → setIsSubmitting(false)
```

### Register Flow

```
User fills form → Submit
  → Client validation (name, email, password, confirmPassword)
  → If invalid → Show inline errors below fields
  → If valid → setIsSubmitting(true) → clearError()
    → useAuth().register(name, email, password)
      → authService.register() → POST /api/v1/auth/register
      → Success → returns true → router.push("/dashboard")
      → Failure → sets error in AuthContext → returns false
        → AuthAlert renders error message
    → setIsSubmitting(false)
```

### Logout Flow

```
User clicks avatar in AppHeader → Dropdown opens
  → User clicks "Sair"
    → useAuth().logout() → Clears tokens and state
    → router.push("/login")
```

---

## Implementation Order

1. **`components/auth/AuthAlert.tsx`** - No dependencies, simple component
2. **`components/auth/LoginForm.tsx`** - Depends on AuthAlert, useAuth, UI components
3. **`components/auth/RegisterForm.tsx`** - Same dependencies as LoginForm
4. **`components/auth/index.ts`** - Barrel export
5. **`app/(auth)/layout.tsx`** - Split screen layout
6. **`app/(auth)/login/page.tsx`** - Renders LoginForm
7. **`app/(auth)/register/page.tsx`** - Renders RegisterForm
8. **`components/app/AppHeader.tsx`** - Add dropdown with logout
9. **Verify AuthContext** - Ensure login/register return boolean, clearError exists

---

## Styling Reference

All components follow these established patterns:

- **forwardRef** for UI primitives (AuthAlert)
- **cn()** utility for Tailwind class merging
- **"use client"** directive for components with hooks
- **Color palette**: primary (green), accent (purple), neutral (grays), red (errors), amber (warnings)
- **Typography**: `font-display` for headings, `font-body` (default) for text
- **Spacing**: Tailwind default scale
- **Border radius**: `rounded-lg` (12px) for inputs, `rounded-xl` (16px) for cards/containers
- **Shadows**: `shadow-soft`, `shadow-medium`, `shadow-elevated`
- **Transitions**: `transition-all duration-300`
- **Animations**: `animate-fadeIn`, `animate-fadeInUp`

---

## Backlog Items (Not in Phase 2)

- **Sandbox mode**: "Comecar gratis" leads to demo dashboard with mock data in localStorage
- **"Esqueceu a senha?"**: Password reset flow (requires backend endpoint)
- **Social login**: Google/GitHub OAuth (requires backend integration)
- **Route protection**: Phase 3 in the original plan
