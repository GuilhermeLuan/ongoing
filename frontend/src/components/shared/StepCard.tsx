import { cn } from "@/lib/utils";
import { LucideIcon } from "lucide-react";

interface StepCardProps {
  step: number;
  icon: LucideIcon;
  title: string;
  description: string;
  className?: string;
}

export function StepCard({
  step,
  icon: Icon,
  title,
  description,
  className,
}: StepCardProps) {
  return (
    <div className={cn("relative text-center", className)}>
      <div className="relative inline-flex mb-6">
        <div className="absolute -inset-4 bg-primary-100 rounded-full opacity-50" />
        <div className="relative w-16 h-16 rounded-full bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center shadow-glow">
          <Icon size={28} className="text-white" />
        </div>
        <div className="absolute -top-2 -right-2 w-8 h-8 rounded-full bg-accent-500 flex items-center justify-center text-white font-bold text-sm shadow-md">
          {step}
        </div>
      </div>

      <h3 className="font-display text-xl font-bold text-neutral-900 mb-2">
        {title}
      </h3>
      <p className="text-neutral-600 leading-relaxed max-w-xs mx-auto">
        {description}
      </p>
    </div>
  );
}
