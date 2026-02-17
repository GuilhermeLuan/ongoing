"use client";

import {useEffect, useState} from "react";
import {Badge} from "@/components/ui";
import {
    billingCycleLabels,
    formatDate,
    formatSubscriptionValue,
    getDaysUntilBilling,
} from "@/features/subscriptions/utils/subscription.utils";
import type {SubscriptionResponse} from "@/features/subscriptions/types/subscription.types";
import {cn} from "@/lib/utils";

interface SubscriptionCardProps {
    subscription: SubscriptionResponse;
  variant?: "default" | "compact";
  className?: string;
  onClick?: () => void;
    onEdit?: (subscription: SubscriptionResponse) => void;
    onDelete?: (subscription: SubscriptionResponse) => void;
}

const avatarColors = [
    "#E50914",
    "#1DB954",
    "#5865F2",
    "#0078D4",
    "#FF6600",
    "#10A37F",
    "#6B7280",
];

const getAvatarColor = (name: string): string => {
    const hash = name.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return avatarColors[hash % avatarColors.length];
};

export function SubscriptionCard({
  subscription,
  variant = "default",
  className,
  onClick,
                                     onEdit,
                                     onDelete,
}: SubscriptionCardProps) {
    const [imageError, setImageError] = useState(false);
    const daysUntil = getDaysUntilBilling(subscription.nextPaymentDate);
  const isUrgent = daysUntil <= 3 && daysUntil >= 0;
    const avatarColor = getAvatarColor(subscription.name);
    const showLogo = subscription.logoUrl && !imageError;

    // Reset image error when subscription changes
    useEffect(() => {
        setImageError(false);
    }, [subscription.logoUrl]);

  if (variant === "compact") {
    return (
      <div
        className={cn(
            "flex cursor-pointer items-center gap-4 rounded-lg border border-neutral-100 bg-white p-3 transition-[border-color,box-shadow] hover:border-neutral-200",
          className
        )}
        onClick={onClick}
      >
          {showLogo ? (
              <img
                  src={subscription.logoUrl ?? undefined}
                  alt={subscription.name}
                  className="w-10 h-10 rounded-lg object-cover shadow-soft"
                  onError={() => setImageError(true)}
              />
          ) : (
              <div
                  className="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm shadow-soft"
                  style={{backgroundColor: avatarColor}}
              >
                  {subscription.name.slice(0, 2).toUpperCase()}
              </div>
          )}
        <div className="flex-1 min-w-0">
            <p className="font-medium text-neutral-900 truncate">{subscription.name}</p>
          <p className="text-sm text-neutral-500">
              {formatDate(subscription.nextPaymentDate)}
          </p>
        </div>
          <div className="min-w-[96px] text-right">
          <p className="font-semibold text-neutral-900">
              {formatSubscriptionValue(subscription.value, subscription.currency)}
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
          "group rounded-xl border border-neutral-100 bg-white p-5 transition-[border-color,box-shadow] hover:border-neutral-200 hover:shadow-medium",
          onClick && "cursor-pointer",
        className
      )}
      onClick={onClick}
    >
      <div className="flex items-start gap-4">
          {showLogo ? (
              <img
                  src={subscription.logoUrl ?? undefined}
                  alt={subscription.name}
                  className="w-12 h-12 rounded-xl object-cover shadow-soft"
                  onError={() => setImageError(true)}
              />
          ) : (
              <div
                  className="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold shadow-soft"
                  style={{backgroundColor: avatarColor}}
              >
                  {subscription.name.slice(0, 2).toUpperCase()}
              </div>
          )}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
              <h3 className="font-semibold text-neutral-900 truncate">{subscription.name}</h3>
              {!subscription.active && <Badge variant="neutral">Inativa</Badge>}
          </div>
            <p className="text-sm text-neutral-500 mt-0.5 line-clamp-1">
                {subscription.description || "Sem descrição"}
          </p>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-neutral-900">
              {formatSubscriptionValue(subscription.value, subscription.currency)}
          </p>
          <p className="text-xs text-neutral-400">
            {billingCycleLabels[subscription.billingCycle]}
          </p>
        </div>
      </div>

        <div className="mt-4 pt-4 border-t border-neutral-100 flex items-center justify-between gap-3">
        <div>
          <p className="text-xs text-neutral-400">Próxima cobrança</p>
          <p className="text-sm font-medium text-neutral-700">
              {formatDate(subscription.nextPaymentDate)}
          </p>
        </div>

            <div className="flex items-center gap-2">
                {daysUntil >= 0 && (
                    <div
                        className={cn(
                            "px-3 py-1.5 rounded-full text-xs font-medium",
                            isUrgent ? "bg-amber-50 text-amber-700" : "bg-neutral-100 text-neutral-600"
                        )}
                    >
                        {daysUntil === 0
                            ? "Vence hoje"
                            : daysUntil === 1
                                ? "Vence amanhã"
                                : `Em ${daysUntil} dias`}
                    </div>
                )}

                {onEdit && (
                    <button
                        type="button"
                        className="text-xs font-medium text-primary-700 hover:text-primary-800"
                        onClick={(event) => {
                            event.stopPropagation();
                            onEdit(subscription);
                        }}
                    >
                        Editar
                    </button>
                )}

                {onDelete && (
                    <button
                        type="button"
                        className="text-xs font-medium text-red-600 hover:text-red-700"
                        onClick={(event) => {
                            event.stopPropagation();
                            onDelete(subscription);
                        }}
                    >
                        Excluir
                    </button>
                )}
            </div>
      </div>
    </div>
  );
}
