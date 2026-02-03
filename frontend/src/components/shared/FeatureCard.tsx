import { cn } from "@/lib/utils";
import { Card } from "@/components/ui";
import { LucideIcon } from "lucide-react";

interface FeatureCardProps {
  icon: LucideIcon;
  title: string;
  description: string;
  className?: string;
}

export function FeatureCard({
  icon: Icon,
  title,
  description,
  className,
}: FeatureCardProps) {
  return (
    <Card
      className={cn("group", className)}
      variant="bordered"
    >
      <div className="mb-4 inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary-100 text-primary-600 transition-all duration-300 group-hover:bg-primary-500 group-hover:text-white group-hover:shadow-glow">
        <Icon size={24} />
      </div>
      <h3 className="font-display text-lg font-semibold text-neutral-900 mb-2">
        {title}
      </h3>
      <p className="text-neutral-600 leading-relaxed">{description}</p>
    </Card>
  );
}
