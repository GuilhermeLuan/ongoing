# Mobile Performance & Responsiveness Review

**Date**: 2026-02-17
**Branch**: fix/mobile-performance
**Status**: Approved

## Problem Statement

The frontend has several mobile-specific issues:

1. **Black bar at top**: Visible on both dashboard and subscription modal (iOS Safari)
2. **Modal freeze**: Screen freezes ~200-500ms when opening subscription creation modal on mobile
3. **General mobile performance**: Heavy GPU effects (backdrop-blur, blur-3xl, infinite animations) degrade experience

## Root Cause Analysis

### Black Bar

- Missing `viewport-fit=cover` in viewport meta tag
- Fixed-positioned elements (Modal, Sidebar, AppHeader) don't account for iOS safe areas (`env(safe-area-inset-*)`)
- Modal backdrop `bg-neutral-900/50` "leaks" into the gap

### Modal Freeze

- `document.body.style.overflow = "hidden"` triggers expensive layout recalculation on mobile Safari
- Two simultaneous animations: `animate-fadeIn` (backdrop) + `animate-fadeInUp` (content)
- `backdrop-blur-sm` on AppHeader beneath modal forces GPU compositing during modal render

### General Performance

| Issue                         | Location                      | Impact                     |
|-------------------------------|-------------------------------|----------------------------|
| `backdrop-blur-md`            | Header (marketing), AppHeader | GPU-heavy on mobile        |
| `animate-float` infinite      | Hero blobs, DashboardPreview  | Constant CPU usage         |
| `blur-3xl` on large divs      | Hero section (w-72, w-96)     | GPU overload               |
| `scroll-behavior: smooth`     | globals.css                   | Scroll jank                |
| `transition-all duration-300` | `.hover-lift`, `.card`        | Transitions ALL properties |
| Complex box-shadows           | soft, medium, elevated        | Constant repaint           |

## Implementation Plan

### Phase 1 - Critical Fixes (Black bar + Freeze)

#### 1.1 Viewport & Safe Areas

- Add `viewport-fit=cover` via Next.js `viewport` export in `app/layout.tsx`
- Add `padding: env(safe-area-inset-*)` to fixed elements:
    - `Modal.tsx` - backdrop and content
    - `Sidebar.tsx` - fixed sidebar
    - `AppHeader.tsx` - sticky header

#### 1.2 Modal Mobile Fix

- Replace `overflow: hidden` scroll lock with `position: fixed` technique (preserves scroll position)
- Simplify modal animations on mobile: opacity-only transition (no translateY)
- Add `will-change: transform` to modal container
- Consider `size="md"` or responsive sizing for SubscriptionForm on mobile

#### 1.3 SubscriptionForm Responsiveness

- Ensure modal uses appropriate size on mobile
- Improve internal scroll behavior within the form

### Phase 2 - Performance (Blur & Animations)

#### 2.1 Backdrop-blur Optimization

- AppHeader: replace `backdrop-blur-sm` with solid `bg-white` on mobile
- Marketing Header: replace `backdrop-blur-md` with solid background on mobile
- Modal backdrop: solid color only (no blur)

#### 2.2 Infinite Animations

- Hero blobs: disable `animate-float` on mobile (`md:animate-float`)
- Hero blobs: reduce `blur-3xl` to `blur-xl` or hide on mobile
- DashboardPreview: remove float animation on mobile
- Add global `prefers-reduced-motion` support

#### 2.3 Transition Optimization

- `.hover-lift` / `.card`: change `transition-all` to `transition-[transform,shadow]`
- `scroll-behavior: smooth`: apply only on `md:` via media query
- Dashboard progress bars: change `transition-all` to `transition-[width]`

### Phase 3 - Responsiveness (Logged-in Area)

#### 3.1 Dashboard

- Review stats grid on mobile (check for horizontal overflow)
- Verify card padding on small screens

#### 3.2 Sidebar

- Verify z-index stacking on mobile (sidebar overlay vs modal)
- Test sidebar + modal interaction

#### 3.3 Subscription List

- Verify SubscriptionCard on mobile (text truncation, layout)

### Phase 4 - Documentation

#### 4.1 Frontend CLAUDE.md

- Create `frontend/CLAUDE.md` with mobile-first guidelines
- Include rules for: performance, responsiveness, safe areas, animations

#### 4.2 FOR-Guilherme.md

- Update with mobile performance lessons learned

## Key Files to Modify

| File                                     | Changes                                              |
|------------------------------------------|------------------------------------------------------|
| `app/layout.tsx`                         | viewport-fit=cover                                   |
| `components/ui/Modal.tsx`                | Scroll lock, animations, safe areas                  |
| `components/app/Sidebar.tsx`             | Safe areas                                           |
| `components/app/AppHeader.tsx`           | Safe areas, remove blur on mobile                    |
| `components/layout/Header.tsx`           | Remove blur on mobile                                |
| `components/sections/Hero.tsx`           | Disable heavy effects on mobile                      |
| `components/shared/DashboardPreview.tsx` | Remove float on mobile                               |
| `app/globals.css`                        | scroll-behavior, transitions, prefers-reduced-motion |
| `tailwind.config.ts`                     | Possibly add reduced-motion variants                 |
| `frontend/CLAUDE.md`                     | New file with mobile guidelines                      |
