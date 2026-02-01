import { cn } from "@/lib/utils";
import { HTMLAttributes, forwardRef } from "react";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "elevated" | "bordered";
  hover?: boolean;
}

const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ className, variant = "default", hover = true, children, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          "rounded-xl bg-white p-6 transition-all duration-300",
          {
            "shadow-soft": variant === "default",
            "shadow-medium": variant === "elevated",
            "border border-neutral-200": variant === "bordered",
            "hover:-translate-y-1 hover:shadow-elevated": hover,
          },
          className
        )}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = "Card";

export { Card };
