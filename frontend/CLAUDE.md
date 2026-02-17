# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ongoing is a subscription management SaaS platform, built with Next.js 14 (App Router) and Tailwind CSS. The frontend has two main areas: a public marketing landing page and an authenticated dashboard application. All content is in Brazilian Portuguese.

## Commands

```bash
npm run dev      # Start development server at localhost:3000
npm run build    # Production build
npm run lint     # Run ESLint
```

## Architecture

### Two-Area App Structure

The app uses **Next.js Route Groups** to separate concerns:

- **`(marketing)/`** — Public landing page at `/`. Uses Header + Footer layout.
- **`(app)/`** — Authenticated dashboard at `/dashboard`, `/subscriptions`. Uses Sidebar layout with SidebarProvider context.

### Layer Documentation

Each `src/` directory has its own CLAUDE.md with detailed documentation:

| Layer | CLAUDE.md | Contents |
|-------|-----------|----------|
| `src/components/` | [components/CLAUDE.md](src/components/CLAUDE.md) | 5-tier component hierarchy (ui, shared, layout, sections, app), all props and variants |
| `src/hooks/` | [hooks/CLAUDE.md](src/hooks/CLAUDE.md) | useScrollAnimation hook API and usage patterns |
| `src/lib/` | [lib/CLAUDE.md](src/lib/CLAUDE.md) | cn() utility, mock data types, label maps, helper functions |
| `src/app/` | [app/CLAUDE.md](src/app/CLAUDE.md) | Route groups, layouts, pages, data flow |

### Component Organization (5 tiers)

Components follow a five-tier hierarchy under `src/components/`:

1. **ui/** (9) — Primitive reusable: Button, Card, Badge, Input, Select, Avatar, Container, GradientText, Skeleton
2. **shared/** (6) — Domain-specific reusable: Logo, FeatureCard, PricingCard, TestimonialCard, StepCard, DashboardPreview
3. **layout/** (2) — Page structure: Header (sticky, mobile menu), Footer (dark bg, 3 link columns)
4. **sections/** (6) — Landing page sections: Hero, Features, HowItWorks, Pricing, Testimonials, FinalCTA
5. **app/** (6) — Dashboard: Sidebar, SidebarContext, AppHeader, StatCard, SubscriptionCard, SubscriptionList

Each tier has an `index.ts` barrel export. Import from the tier, not individual files:
```typescript
import { Button, Card } from "@/components/ui";
import { Header, Footer } from "@/components/layout";
import { Sidebar, AppHeader } from "@/components/app";
```

### Styling Approach

- Uses `cn()` utility from `@/lib/utils` for merging Tailwind classes (clsx + tailwind-merge)
- Custom color palette in `tailwind.config.ts`: `primary` (green), `accent` (purple), `neutral`
- Three font families via CSS variables: `font-display` (Plus Jakarta Sans), `font-body` (Inter), `font-mono` (JetBrains Mono)
- Custom animations defined in Tailwind config: `fadeIn`, `fadeInUp`, `scaleIn`, `slideInLeft`, `slideInRight`, `float`
- Custom shadows: `soft`, `medium`, `elevated`, `glow`, `glow-accent`
- Icon library: **Lucide React**

### Mobile-first Performance Guidelines

- Always keep `viewportFit: "cover"` in `src/app/layout.tsx` and account for iOS safe areas (`env(safe-area-inset-*)`)
  in fixed/sticky UI (Modal, Sidebar, headers).
- Prefer solid backgrounds on mobile over `backdrop-blur-*`; if blur is needed, gate it behind `md:`.
- Avoid infinite animations on mobile. Use responsive classes (e.g., `md:animate-*`) and keep decorative blur elements
  lighter on small screens.
- Avoid `transition-all` in interactive components. Transition only required properties (`transform`, `box-shadow`,
  `width`, `colors`).
- Keep modal open/close lightweight on mobile: lock scroll with `position: fixed` preserving scroll offset, and keep
  content scrollable inside modal.
- Respect `prefers-reduced-motion` in global CSS and disable smooth scroll for users requesting reduced motion.

### Design System Tokens

```
Colors:    primary-{50-950} (green), accent-{50-950} (purple), neutral-{50-950}
Shadows:   soft, medium, elevated, glow, glow-accent
Radii:     default (8px), lg (12px), xl (16px), 2xl (24px)
Container: sm (3xl), md (5xl), lg (7xl), xl (1400px)
```

### Data Flow

Currently uses **mock data** from `src/lib/mock-data.ts` (12 subscriptions, pre-calculated stats). Backend integration via `NEXT_PUBLIC_API_URL` is planned.

### Path Aliases

`@/*` maps to `./src/*`
