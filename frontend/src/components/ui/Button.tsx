import { cn } from "@/lib/utils";
import { ButtonHTMLAttributes, forwardRef } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "ghost";
  size?: "sm" | "md" | "lg";
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "inline-flex items-center justify-center font-semibold transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed",
          {
            // Variants
            "bg-primary-500 text-white hover:bg-primary-600 focus:ring-primary-500 shadow-soft hover:shadow-glow":
              variant === "primary",
            "bg-neutral-900 text-white hover:bg-neutral-800 focus:ring-neutral-500":
              variant === "secondary",
            "border-2 border-primary-500 text-primary-600 hover:bg-primary-50 focus:ring-primary-500":
              variant === "outline",
            "text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 focus:ring-neutral-500":
              variant === "ghost",
            // Sizes
            "text-sm px-4 py-2 rounded-lg": size === "sm",
            "text-base px-6 py-3 rounded-lg": size === "md",
            "text-lg px-8 py-4 rounded-xl": size === "lg",
          },
          className
        )}
        {...props}
      >
        {children}
      </button>
    );
  }
);

Button.displayName = "Button";

export { Button };
