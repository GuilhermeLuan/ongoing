import { cn } from "@/lib/utils";
import { Repeat } from "lucide-react";

interface LogoProps {
  className?: string;
  size?: "sm" | "md" | "lg";
  showText?: boolean;
}

export function Logo({ className, size = "md", showText = true }: LogoProps) {
  const sizes = {
    sm: { icon: 20, text: "text-lg" },
    md: { icon: 24, text: "text-xl" },
    lg: { icon: 32, text: "text-2xl" },
  };

  return (
    <div className={cn("flex items-center gap-2", className)}>
      <div className="relative">
        <div className="absolute inset-0 bg-primary-500 blur-lg opacity-30 rounded-full" />
        <div className="relative bg-gradient-to-br from-primary-500 to-primary-600 p-2 rounded-xl">
          <Repeat
            size={sizes[size].icon}
            className="text-white"
            strokeWidth={2.5}
          />
        </div>
      </div>
      {showText && (
        <span
          className={cn(
            "font-display font-bold text-neutral-900",
            sizes[size].text
          )}
        >
          Ongoing
        </span>
      )}
    </div>
  );
}
