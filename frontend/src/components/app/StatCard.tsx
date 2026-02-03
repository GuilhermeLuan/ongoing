import { cn } from "@/lib/utils";

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  variant?: "default" | "primary" | "warning" | "success";
  className?: string;
}

export function StatCard({
  title,
  value,
  subtitle,
  icon,
  trend,
  variant = "default",
  className,
}: StatCardProps) {
  const variantStyles = {
    default: {
      container: "bg-white",
      iconBg: "bg-neutral-100",
      iconColor: "text-neutral-600",
    },
    primary: {
      container: "bg-gradient-to-br from-primary-500 to-primary-600 text-white",
      iconBg: "bg-white/20",
      iconColor: "text-white",
    },
    warning: {
      container: "bg-white",
      iconBg: "bg-amber-100",
      iconColor: "text-amber-600",
    },
    success: {
      container: "bg-white",
      iconBg: "bg-emerald-100",
      iconColor: "text-emerald-600",
    },
  };

  const styles = variantStyles[variant];

  return (
    <div
      className={cn(
        "rounded-xl p-5 border border-neutral-100 shadow-soft",
        styles.container,
        className
      )}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p
            className={cn(
              "text-sm font-medium",
              variant === "primary" ? "text-white/80" : "text-neutral-500"
            )}
          >
            {title}
          </p>
          <p
            className={cn(
              "text-2xl font-bold mt-1",
              variant === "primary" ? "text-white" : "text-neutral-900"
            )}
          >
            {value}
          </p>
          {subtitle && (
            <p
              className={cn(
                "text-sm mt-1",
                variant === "primary" ? "text-white/70" : "text-neutral-400"
              )}
            >
              {subtitle}
            </p>
          )}
          {trend && (
            <div
              className={cn(
                "flex items-center gap-1 mt-2 text-sm font-medium",
                trend.isPositive ? "text-emerald-500" : "text-red-500"
              )}
            >
              <svg
                className={cn("w-4 h-4", !trend.isPositive && "rotate-180")}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 10l7-7m0 0l7 7m-7-7v18"
                />
              </svg>
              <span>{Math.abs(trend.value)}%</span>
            </div>
          )}
        </div>
        <div className={cn("p-3 rounded-lg", styles.iconBg)}>
          <span className={styles.iconColor}>{icon}</span>
        </div>
      </div>
    </div>
  );
}
