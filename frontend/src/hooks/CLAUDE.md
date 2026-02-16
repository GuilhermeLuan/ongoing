# Hooks

## useScrollAnimation

IntersectionObserver-based hook for scroll-triggered animations. Used by all `sections/` components.

### Signature

```typescript
function useScrollAnimation<T extends HTMLElement = HTMLDivElement>(
  options?: UseScrollAnimationOptions
): { ref: RefObject<T>; isVisible: boolean }
```

### Options

| Option        | Type    | Default | Description                                                   |
|---------------|---------|---------|---------------------------------------------------------------|
| `threshold`   | number  | 0.1     | Intersection ratio to trigger (0-1)                           |
| `rootMargin`  | string  | "0px"   | Observer root margin                                          |
| `triggerOnce` | boolean | true    | If true, stays visible after first trigger; if false, toggles |

### Usage Pattern

```typescript
"use client";
import { useScrollAnimation } from "@/hooks/useScrollAnimation";
import { cn } from "@/lib/utils";

function MySection() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section>
      <div ref={ref}>
        <h2 className={cn("opacity-0", isVisible && "animate-fadeInUp")}>
          Title
        </h2>
      </div>
    </section>
  );
}
```

### Staggered Children Pattern

For multiple items entering sequentially:

```typescript
{items.map((item, index) => (
  <div
    className={cn("opacity-0", isVisible && "animate-fadeInUp")}
    style={{ animationDelay: isVisible ? `${(index + 2) * 100}ms` : "0ms" }}
  >
    <ItemComponent {...item} />
  </div>
))}
```

### Notes

- Requires `"use client"` directive (uses useEffect, useRef, useState)
- The `ref` must be attached to a DOM element for observation
- Available animations from Tailwind config: `animate-fadeIn`, `animate-fadeInUp`, `animate-scaleIn`,
  `animate-slideInLeft`, `animate-slideInRight`
- Elements start with `opacity-0` and the animation class is conditionally added when `isVisible` becomes true
