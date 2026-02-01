import { cn } from "@/lib/utils";
import { HTMLAttributes, forwardRef } from "react";

interface GradientTextProps extends HTMLAttributes<HTMLSpanElement> {
  as?: "span" | "h1" | "h2" | "h3" | "p";
}

const GradientText = forwardRef<HTMLSpanElement, GradientTextProps>(
  ({ className, as: Component = "span", children, ...props }, ref) => {
    return (
      <Component
        ref={ref as never}
        className={cn(
          "bg-gradient-to-r from-primary-500 to-accent-500 bg-clip-text text-transparent",
          className
        )}
        {...props}
      >
        {children}
      </Component>
    );
  }
);

GradientText.displayName = "GradientText";

export { GradientText };
