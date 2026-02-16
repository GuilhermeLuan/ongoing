# App (Next.js App Router)

## Root Layout (`layout.tsx`)

- **Language**: `pt-BR`
- **Fonts** (CSS variables → Tailwind classes):
    - `--font-plus-jakarta` → `font-display` (Plus Jakarta Sans) — headings
    - `--font-inter` → `font-body` (Inter) — body text, default
    - `--font-jetbrains` → `font-mono` (JetBrains Mono) — numbers, code
- **SEO**: Portuguese metadata, OpenGraph with locale `pt_BR`
- **Global**: `scroll-smooth` on html, `antialiased` on body

## Route Groups

### `(marketing)/` — Public Landing Page

**Layout**: Wraps children with `<Header />` + `<Footer />` from `@/components/layout`.

**Page** (`page.tsx`): Server component. Renders all 6 sections in order:

1. `<Hero />`
2. `<Features />`
3. `<HowItWorks />`
4. `<Pricing />`
5. `<Testimonials />`
6. `<FinalCTA />`

**Route**: `/` (root URL)

### `(app)/` — Authenticated Dashboard

**Layout**: "use client". Wraps children with:

- `<SidebarProvider>` — Context for sidebar open/close state
- `<Sidebar>` — Fixed left navigation (w-60)
- Content area with `lg:pl-60` padding to account for sidebar
- Background: `bg-neutral-50`

**Pages**:

| Route            | File                     | Title                   | Content                                                                                                                            |
|------------------|--------------------------|-------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `/dashboard`     | `dashboard/page.tsx`     | "Dashboard - Ongoing"   | Server component. AppHeader, 4 StatCards grid, upcoming billings (SubscriptionCard compact), category breakdown with progress bars |
| `/subscriptions` | `subscriptions/page.tsx` | "Assinaturas - Ongoing" | Server component. AppHeader with subtitle, "Adicionar" button, SubscriptionList with all mock subscriptions                        |

**Planned routes** (sidebar links exist but pages not yet created):

- `/categories`
- `/calendar`
- `/settings`

## Data Flow (Current)

All pages currently use mock data from `@/lib/mock-data`. The dashboard page calls `getUpcomingBillings()` and reads
`mockStats`. The subscriptions page passes `mockSubscriptions` to `SubscriptionList`.

## Adding New Pages

1. Create folder under `(marketing)/` for public pages or `(app)/` for authenticated pages
2. Add `page.tsx` with `export const metadata` for SEO
3. For `(app)` pages, include `<AppHeader />` at the top and wrap content in `<main className="p-6">`
4. Update Sidebar nav links in `components/app/Sidebar.tsx`
