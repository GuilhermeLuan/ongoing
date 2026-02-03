# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ongoing is a landing page for a subscription management SaaS product, built with Next.js 14 (App Router) and Tailwind CSS. The content is in Brazilian Portuguese.

## Commands

```bash
npm run dev      # Start development server at localhost:3000
npm run build    # Production build
npm run lint     # Run ESLint
```

## Architecture

### Component Organization

Components follow a three-tier structure under `src/components/`:

- **ui/** - Primitive reusable components (Button, Card, Badge, Container, GradientText)
- **shared/** - Domain-specific reusable components (Logo, FeatureCard, PricingCard, TestimonialCard, StepCard, DashboardPreview)
- **layout/** - Page structure components (Header, Footer)
- **sections/** - Full landing page sections (Hero, Features, HowItWorks, Pricing, Testimonials, FinalCTA)

Each tier has an `index.ts` barrel export. Import from the tier, not individual files:
```typescript
import { Button, Card } from "@/components/ui";
import { Header, Footer } from "@/components/layout";
```

### Styling Approach

- Uses `cn()` utility from `@/lib/utils` for merging Tailwind classes (clsx + tailwind-merge)
- Custom color palette in `tailwind.config.ts`: `primary` (green), `accent` (purple), `neutral`
- Three font families via CSS variables: `font-display` (Plus Jakarta Sans), `font-body` (Inter), `font-mono` (JetBrains Mono)
- Custom animations defined in Tailwind config: `fadeIn`, `fadeInUp`, `scaleIn`, `slideInLeft`, `slideInRight`, `float`
- Custom shadows: `soft`, `medium`, `elevated`, `glow`, `glow-accent`

### Path Aliases

`@/*` maps to `./src/*`
