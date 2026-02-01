import { cn } from "@/lib/utils";
import { Button, Badge } from "@/components/ui";
import { Check } from "lucide-react";

interface PricingCardProps {
  name: string;
  price: string;
  period?: string;
  description: string;
  features: string[];
  highlighted?: boolean;
  badge?: string;
  className?: string;
}

export function PricingCard({
  name,
  price,
  period = "/mês",
  description,
  features,
  highlighted = false,
  badge,
  className,
}: PricingCardProps) {
  return (
    <div
      className={cn(
        "relative rounded-2xl p-8 transition-all duration-300",
        highlighted
          ? "bg-gradient-to-b from-primary-500 to-primary-600 text-white shadow-elevated scale-105 z-10"
          : "bg-white border border-neutral-200 hover:border-primary-200 hover:shadow-medium",
        className
      )}
    >
      {badge && (
        <Badge
          variant={highlighted ? "neutral" : "primary"}
          className="absolute -top-3 left-1/2 -translate-x-1/2"
        >
          {badge}
        </Badge>
      )}

      <div className="text-center mb-6">
        <h3
          className={cn(
            "font-display text-xl font-bold mb-2",
            highlighted ? "text-white" : "text-neutral-900"
          )}
        >
          {name}
        </h3>
        <p
          className={cn(
            "text-sm mb-4",
            highlighted ? "text-primary-100" : "text-neutral-500"
          )}
        >
          {description}
        </p>
        <div className="flex items-baseline justify-center gap-1">
          <span
            className={cn(
              "font-mono text-4xl font-bold",
              highlighted ? "text-white" : "text-neutral-900"
            )}
          >
            {price}
          </span>
          {price !== "Grátis" && (
            <span
              className={cn(
                "text-sm",
                highlighted ? "text-primary-100" : "text-neutral-500"
              )}
            >
              {period}
            </span>
          )}
        </div>
      </div>

      <ul className="space-y-3 mb-8">
        {features.map((feature, index) => (
          <li key={index} className="flex items-start gap-3">
            <div
              className={cn(
                "flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center mt-0.5",
                highlighted ? "bg-white/20" : "bg-primary-100"
              )}
            >
              <Check
                size={12}
                className={highlighted ? "text-white" : "text-primary-600"}
              />
            </div>
            <span
              className={cn(
                "text-sm",
                highlighted ? "text-primary-50" : "text-neutral-600"
              )}
            >
              {feature}
            </span>
          </li>
        ))}
      </ul>

      <Button
        variant={highlighted ? "secondary" : "outline"}
        className="w-full"
        size="lg"
      >
        {price === "Grátis" ? "Começar grátis" : "Assinar agora"}
      </Button>
    </div>
  );
}
