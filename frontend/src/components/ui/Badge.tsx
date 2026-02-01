import { cn } from "@/lib/utils";
import { HTMLAttributes, forwardRef } from "react";

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: "primary" | "accent" | "neutral" | "success";
}

const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className, variant = "primary", children, ...props }, ref) => {
    return (
      <span
        ref={ref}
        className={cn(
          "inline-flex items-center px-3 py-1 text-sm font-medium rounded-full",
          {
            "bg-primary-100 text-primary-700": variant === "primary",
            "bg-accent-100 text-accent-700": variant === "accent",
            "bg-neutral-100 text-neutral-700": variant === "neutral",
            "bg-green-100 text-green-700": variant === "success",
          },
          className
        )}
        {...props}
      >
        {children}
      </span>
    );
  }
);

Badge.displayName = "Badge";

export { Badge };
