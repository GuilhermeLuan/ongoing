"use client";

import {useEffect, useState} from "react";
import {Bell, Calendar, CreditCard, Grid3x3} from "lucide-react";
import {Badge, Button} from "@/components/ui";
import type {SubscriptionResponse} from "@/features/subscriptions";
import {
    billingCycleLabels,
    formatDate,
    formatSubscriptionValue,
    getCategoryName,
    getDaysUntilBilling,
    getPaymentMethodName,
} from "@/features/subscriptions";

interface SubscriptionDetailsViewProps {
    subscription: SubscriptionResponse;
    onEdit: () => void;
    onToggleActive: () => void;
    onDelete: () => void;
    onClose: () => void;
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

export function SubscriptionDetailsView({
                                            subscription,
                                            onEdit,
                                            onToggleActive,
                                            onDelete,
                                        }: SubscriptionDetailsViewProps) {
    const [imageError, setImageError] = useState(false);
    const avatarColor = getAvatarColor(subscription.name);
    const showLogo = subscription.logoUrl && !imageError;
    const daysUntil = getDaysUntilBilling(subscription.nextPaymentDate);

    // Reset image error when subscription changes
    useEffect(() => {
        setImageError(false);
    }, [subscription.logoUrl]);

    const getDaysUntilText = (days: number): string => {
        if (days === 0) return "Hoje";
        if (days === 1) return "Amanhã";
        if (days < 0) return `${Math.abs(days)} dias atrás`;
        return `Em ${days} dias`;
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-start gap-4">
                <div className="flex-1">
                    <div className="flex items-center gap-4 mb-3">
                        {showLogo ? (
                            <img
                                src={subscription.logoUrl ?? undefined}
                                alt={subscription.name}
                                className="w-16 h-16 rounded-2xl object-cover shadow-soft"
                                onError={() => setImageError(true)}
                            />
                        ) : (
                            <div
                                className="w-16 h-16 rounded-2xl flex items-center justify-center text-white font-bold text-xl shadow-soft"
                                style={{backgroundColor: avatarColor}}
                            >
                                {subscription.name.slice(0, 2).toUpperCase()}
                            </div>
                        )}
                        <div>
                            <h2 className="text-2xl font-bold text-neutral-900">{subscription.name}</h2>
                            <p className="text-sm text-neutral-500 mt-0.5">
                                Próxima cobrança: {formatDate(subscription.nextPaymentDate)}
                            </p>
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-2">
                    <Button
                        onClick={onEdit}
                        size="sm"
                        variant="outline"
                    >
                        Editar
                    </Button>
                    <Button
                        onClick={onToggleActive}
                        size="sm"
                        variant={subscription.active ? "outline" : "primary"}
                    >
                        {subscription.active ? "Marcar como cancelada" : "Reativar"}
                    </Button>
                    <Button
                        onClick={onDelete}
                        size="sm"
                        variant="outline"
                        className="text-red-600 hover:text-red-700 border-red-200 hover:bg-red-50"
                    >
                        Excluir
                    </Button>
                </div>
            </div>

            {/* Info Cards Row */}
            <div className="grid grid-cols-3 gap-4">
                {/* Plano/Valor */}
                <div className="p-4 rounded-lg bg-primary-50 border border-primary-100">
                    <p className="text-xs font-medium text-primary-700 uppercase tracking-wide mb-1">
                        Plano
                    </p>
                    <p className="text-2xl font-bold text-primary-900">
                        {formatSubscriptionValue(subscription.value, subscription.currency)}
                    </p>
                    <p className="text-xs text-primary-600 mt-1">
                        {billingCycleLabels[subscription.billingCycle]}
                    </p>
                </div>

                {/* Status */}
                <div className="p-4 rounded-lg bg-neutral-50 border border-neutral-100">
                    <p className="text-xs font-medium text-neutral-600 uppercase tracking-wide mb-2">
                        Status
                    </p>
                    <Badge variant={subscription.active ? "success" : "neutral"}>
                        {subscription.active ? "Ativa" : "Inativa"}
                    </Badge>
                </div>

                {/* Categoria */}
                <div className="p-4 rounded-lg bg-neutral-50 border border-neutral-100">
                    <p className="text-xs font-medium text-neutral-600 uppercase tracking-wide mb-1">
                        Categoria
                    </p>
                    <p className="text-sm font-semibold text-neutral-900">
                        {getCategoryName(subscription)}
                    </p>
                </div>
            </div>

            {/* Detail Rows */}
            <div className="space-y-4 pt-2">
                {/* Próxima cobrança */}
                <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-primary-100 flex items-center justify-center flex-shrink-0">
                        <Calendar className="w-5 h-5 text-primary-700"/>
                    </div>
                    <div className="flex-1">
                        <p className="text-sm font-medium text-neutral-900">Próxima cobrança</p>
                        <p className="text-sm text-neutral-600 mt-0.5">
                            {formatDate(subscription.nextPaymentDate)} · {getDaysUntilText(daysUntil)}
                        </p>
                    </div>
                </div>

                {/* Categoria */}
                <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-accent-100 flex items-center justify-center flex-shrink-0">
                        <Grid3x3 className="w-5 h-5 text-accent-700"/>
                    </div>
                    <div className="flex-1">
                        <p className="text-sm font-medium text-neutral-900">Categoria</p>
                        <p className="text-sm text-neutral-600 mt-0.5">
                            {getCategoryName(subscription)}
                        </p>
                    </div>
                </div>

                {/* Método de pagamento */}
                <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0">
                        <CreditCard className="w-5 h-5 text-blue-700"/>
                    </div>
                    <div className="flex-1">
                        <p className="text-sm font-medium text-neutral-900">Método de pagamento</p>
                        <p className="text-sm text-neutral-600 mt-0.5">
                            {getPaymentMethodName(subscription)}
                        </p>
                    </div>
                </div>

                {/* Lembrete de renovação */}
                <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-amber-100 flex items-center justify-center flex-shrink-0">
                        <Bell className="w-5 h-5 text-amber-700"/>
                    </div>
                    <div className="flex-1">
                        <p className="text-sm font-medium text-neutral-900">Lembrete de renovação</p>
                        <p className="text-sm text-neutral-600 mt-0.5">
                            {subscription.notifyUser ? "Ativado" : "Nenhum"}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
