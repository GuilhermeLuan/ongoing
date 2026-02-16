# Components

Five-tier component hierarchy. Each tier has a barrel `index.ts` — always import from the tier, never from individual
files.

```typescript
import { Button, Card } from "@/components/ui";
import { Logo } from "@/components/shared";
import { Header } from "@/components/layout";
import { Hero } from "@/components/sections";
import { Sidebar } from "@/components/app";
```

## ui/ — Primitive Components (9)

Foundation layer. All use `cn()` from `@/lib/utils` for class merging. Most use `forwardRef`.

| Component        | Props                                                                | Variants/Sizes                                                                        | Notes                                                                          |
|------------------|----------------------------------------------------------------------|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| **Button**       | `variant`, `size` + ButtonHTMLAttributes                             | variant: `primary` \| `secondary` \| `outline` \| `ghost`; size: `sm` \| `md` \| `lg` | forwardRef, default: primary/md                                                |
| **Card**         | `variant`, `hover` + HTMLAttributes                                  | variant: `default` \| `elevated` \| `bordered`; hover: boolean                        | forwardRef, default: default/true                                              |
| **Badge**        | `variant` + HTMLAttributes\<span\>                                   | variant: `primary` \| `accent` \| `neutral` \| `success`                              | forwardRef, renders `<span>`                                                   |
| **Input**        | `label?`, `error?`, `icon?` + InputHTMLAttributes                    | —                                                                                     | forwardRef, auto-generates id from label                                       |
| **Select**       | `label?`, `error?`, `options`, `placeholder?` + SelectHTMLAttributes | —                                                                                     | forwardRef, custom chevron icon, `options: { value: string; label: string }[]` |
| **Avatar**       | `src?`, `alt?`, `fallback?`, `size?`, `className?`                   | size: `sm` \| `md` \| `lg` \| `xl`                                                    | No forwardRef. Shows image or 2-char initials with gradient bg                 |
| **Container**    | `size` + HTMLAttributes                                              | size: `sm` (3xl) \| `md` (5xl) \| `lg` (7xl) \| `xl` (1400px)                         | forwardRef, default: lg                                                        |
| **GradientText** | `as?` + HTMLAttributes\<span\>                                       | as: `span` \| `h1` \| `h2` \| `h3` \| `p`                                             | forwardRef, gradient: primary-500 to accent-500                                |
| **Skeleton**     | `variant?`, `width?`, `height?`, `className?`                        | variant: `text` \| `circular` \| `rectangular`                                        | No forwardRef. width/height accept string or number (px)                       |

## shared/ — Domain Components (6)

Reusable domain-specific components. Used in both landing page and potentially elsewhere.

| Component            | Key Props                                                              | Usage                                                                                |
|----------------------|------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| **Logo**             | `className?`                                                           | Brand logo with icon + text, links to `/`                                            |
| **FeatureCard**      | `icon: LucideIcon`, `title`, `description`                             | Uses Card internally, renders icon in gradient circle                                |
| **PricingCard**      | `name`, `price`, `description`, `features[]`, `highlighted?`, `badge?` | Highlighted variant has primary border + shadow-glow, "Mais popular" badge           |
| **TestimonialCard**  | `name`, `role`, `content`, `rating`, `initials`, `avatarColor`         | Uses Card, shows star rating + avatar initials                                       |
| **StepCard**         | `step: number`, `icon: LucideIcon`, `title`, `description`             | Numbered step with accent badge, centered layout                                     |
| **DashboardPreview** | `className?`                                                           | "use client". Self-contained mock dashboard preview with hardcoded subscription data |

## layout/ — Page Structure (2)

Used in `(marketing)` route group layout.

| Component  | Behavior                                                                                                                                                                   |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Header** | "use client". Sticky (fixed top), scroll detection (bg changes on scroll), mobile hamburger menu (Menu/X toggle), nav links to #sections, CTA buttons link to `/dashboard` |
| **Footer** | Server component. Dark bg (neutral-900), 3 link columns (Produto, Empresa, Legal), social icons (Twitter, Instagram, LinkedIn, GitHub), brand section                      |

## sections/ — Landing Page Sections (6)

Full-page sections. All are "use client" and use `useScrollAnimation` for IntersectionObserver-based entrance
animations.

| Section          | id              | Background                                   | Key Elements                                               |
|------------------|-----------------|----------------------------------------------|------------------------------------------------------------|
| **Hero**         | —               | Gradient (green to white) + decorative blobs | Badge, h1 with GradientText, CTA buttons, DashboardPreview |
| **Features**     | `#features`     | white                                        | 6 FeatureCards in 3-col grid, staggered animation delays   |
| **HowItWorks**   | `#how-it-works` | neutral-50                                   | 3 StepCards, desktop connection line between steps         |
| **Pricing**      | `#pricing`      | white                                        | 3 PricingCards (Gratis, Pro, Business), max-w-5xl centered |
| **Testimonials** | `#testimonials` | neutral-50                                   | 3 TestimonialCards, staggered animation                    |
| **FinalCTA**     | —               | white + gradient blob                        | CTA with benefits list (CheckCircle icons)                 |

**Animation pattern**: Each section does:

```typescript
const { ref, isVisible } = useScrollAnimation();
// ref goes on container, isVisible toggles animate-fadeInUp
// Staggered children use style={{ animationDelay: `${(index + 2) * 100}ms` }}
```

## app/ — Dashboard Components (6)

Used in `(app)` route group. All are "use client" except StatCard.

| Component            | Key Props                                          | Behavior                                                                                                                   |
|----------------------|----------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| **Sidebar**          | `isOpen`, `onClose`                                | Fixed left (w-60), mobile overlay with backdrop, nav links with icons (Lucide), active state detection via `usePathname()` |
| **SidebarContext**   | —                                                  | React Context providing `{ isOpen, open, close, toggle }`, `SidebarProvider` wraps app layout                              |
| **AppHeader**        | `title?`, `subtitle?`                              | Top bar with hamburger toggle (lg:hidden), avatar, notification bell. Uses `useSidebar()`                                  |
| **StatCard**         | `title`, `value`, `subtitle?`, `icon?`, `variant?` | variant: `default` \| `primary` \| `warning` \| `success`. Primary variant has gradient bg                                 |
| **SubscriptionCard** | `subscription: Subscription`, `variant?`           | variant: `default` \| `compact`. Shows color indicator, status badge, billing info, days-until counter                     |
| **SubscriptionList** | `subscriptions: Subscription[]`, `isLoading?`      | Search input + category/status Select filters. Uses `useMemo` for filtered results. Loading skeleton state                 |

## Patterns

- **Barrel exports**: Every tier has `index.ts`. Add new components there.
- **forwardRef**: UI primitives use it. App/shared components generally don't.
- **cn() utility**: All components use `cn()` from `@/lib/utils` (clsx + tailwind-merge).
- **"use client"**: Required for components using hooks (useState, useEffect, useRef, context). Sections, app
  components, Header, DashboardPreview.
- **Lucide React**: Icon library used throughout. Icons are passed as `LucideIcon` type or rendered inline.
- **Variant pattern**: Props with union types control visual variants via `cn()` conditional objects.
