import { cn } from "@/lib/utils";
import {
  Subscription,
  categoryLabels,
  billingCycleLabels,
  formatCurrency,
  formatDate,
  getDaysUntilBilling,
} from "@/lib/mock-data";
import { Badge } from "@/components/ui";

interface SubscriptionCardProps {
  subscription: Subscription;
  variant?: "default" | "compact";
  className?: string;
  onClick?: () => void;
}

export function SubscriptionCard({
  subscription,
  variant = "default",
  className,
  onClick,
}: SubscriptionCardProps) {
  const daysUntil = getDaysUntilBilling(subscription.nextBilling);
  const isUrgent = daysUntil <= 3 && daysUntil >= 0;

  if (variant === "compact") {
    return (
      <div
        className={cn(
          "flex items-center gap-4 p-3 rounded-lg bg-white border border-neutral-100 hover:border-neutral-200 transition-all cursor-pointer",
          className
        )}
        onClick={onClick}
      >
        <div
          className="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm"
          style={{ backgroundColor: subscription.color || "#6B7280" }}
        >
          {subscription.name.slice(0, 2).toUpperCase()}
        </div>
        <div className="flex-1 min-w-0">
          <p className="font-medium text-neutral-900 truncate">
            {subscription.name}
          </p>
          <p className="text-sm text-neutral-500">
            {formatDate(subscription.nextBilling)}
          </p>
        </div>
        <div className="text-right">
          <p className="font-semibold text-neutral-900">
            {formatCurrency(subscription.price)}
          </p>
          {isUrgent && (
            <span className="text-xs text-amber-600 font-medium">
              {daysUntil === 0
                ? "Hoje"
                : daysUntil === 1
                ? "Amanhã"
                : `Em ${daysUntil} dias`}
            </span>
          )}
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        "group p-5 rounded-xl bg-white border border-neutral-100 hover:border-neutral-200 hover:shadow-medium transition-all cursor-pointer",
        className
      )}
      onClick={onClick}
    >
      <div className="flex items-start gap-4">
        <div
          className="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold shadow-soft"
          style={{ backgroundColor: subscription.color || "#6B7280" }}
        >
          {subscription.name.slice(0, 2).toUpperCase()}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h3 className="font-semibold text-neutral-900 truncate">
              {subscription.name}
            </h3>
            {subscription.status === "PAUSED" && (
              <Badge variant="neutral">
                Pausada
              </Badge>
            )}
          </div>
          <p className="text-sm text-neutral-500 mt-0.5">
            {categoryLabels[subscription.category]}
          </p>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-neutral-900">
            {formatCurrency(subscription.price)}
          </p>
          <p className="text-xs text-neutral-400">
            {billingCycleLabels[subscription.billingCycle]}
          </p>
        </div>
      </div>

      <div className="mt-4 pt-4 border-t border-neutral-100 flex items-center justify-between">
        <div>
          <p className="text-xs text-neutral-400">Próxima cobrança</p>
          <p className="text-sm font-medium text-neutral-700">
            {formatDate(subscription.nextBilling)}
          </p>
        </div>
        {daysUntil >= 0 && (
          <div
            className={cn(
              "px-3 py-1.5 rounded-full text-xs font-medium",
              isUrgent
                ? "bg-amber-50 text-amber-700"
                : "bg-neutral-100 text-neutral-600"
            )}
          >
            {daysUntil === 0
              ? "Vence hoje"
              : daysUntil === 1
              ? "Vence amanhã"
              : `Em ${daysUntil} dias`}
          </div>
        )}
      </div>
    </div>
  );
}
