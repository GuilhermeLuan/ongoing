import { cn } from "@/lib/utils";
import { Card } from "@/components/ui";
import { Star } from "lucide-react";

interface TestimonialCardProps {
  name: string;
  role: string;
  content: string;
  rating?: number;
  initials?: string;
  avatarColor?: string;
  className?: string;
}

export function TestimonialCard({
  name,
  role,
  content,
  rating = 5,
  initials,
  avatarColor = "bg-primary-500",
  className,
}: TestimonialCardProps) {
  // Gerar iniciais do nome caso não fornecidas
  const displayInitials = initials || name
    .split(" ")
    .map(word => word[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <Card
      className={cn("group h-full flex flex-col", className)}
      variant="bordered"
    >
      {/* Rating */}
      <div className="flex gap-1 mb-4">
        {Array.from({ length: 5 }).map((_, index) => (
          <Star
            key={index}
            size={16}
            className={cn(
              "transition-colors",
              index < rating
                ? "fill-amber-400 text-amber-400"
                : "text-neutral-300"
            )}
          />
        ))}
      </div>

      {/* Content */}
      <p className="text-neutral-700 leading-relaxed mb-6 flex-grow">
        &ldquo;{content}&rdquo;
      </p>

      {/* Author */}
      <div className="flex items-center gap-3">
        <div
          className={cn(
            "flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold text-sm shadow-medium",
            avatarColor
          )}
        >
          {displayInitials}
        </div>
        <div>
          <div className="font-semibold text-neutral-900">{name}</div>
          <div className="text-sm text-neutral-500">{role}</div>
        </div>
      </div>
    </Card>
  );
}
