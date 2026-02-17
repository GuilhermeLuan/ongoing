"use client";

import {useEffect, useState} from "react";
import {ArrowLeft, Bell} from "lucide-react";
import {Button, Input, Select, Toggle} from "@/components/ui";
import {SubscriptionHeader} from "./SubscriptionHeader";
import {AppearanceEditor} from "./AppearanceEditor";
import {
    type BillingCycle,
    billingCycleLabels,
    calculateNextPaymentDate,
    categoryOptions,
    type Currency,
    currencyLabels,
    paymentMethodOptions,
    type PopularService,
    type SubscriptionRequest,
    type SubscriptionResponse,
} from "@/features/subscriptions";

interface SubscriptionFormProps {
    subscription?: SubscriptionResponse;
    prefill?: PopularService;
    onSubmit: (data: SubscriptionRequest) => Promise<void>;
    onCancel: () => void;
    onBack?: () => void;
    isSubmitting?: boolean;
}

interface FormValues {
    name: string;
    description: string;
    value: string;
    startDate: string;
    categoryId: string;
    paymentMethodId: string;
    currency: Currency;
    billingCycle: BillingCycle;
    active: boolean;
    notifyUser: boolean;
    logoUrl: string;
}

const PRESET_COLORS = [
    "#E50914", "#1DB954", "#5865F2", "#0078D4",
    "#FF6600", "#10A37F", "#FF0080", "#FFC107",
    "#9C27B0", "#00BCD4", "#4CAF50", "#6B7280",
];

const getDefaultColor = (name: string): string => {
    const hash = name.split("").reduce((acc, char) => {
        return char.charCodeAt(0) + ((acc << 5) - acc);
    }, 0);
    return PRESET_COLORS[Math.abs(hash) % PRESET_COLORS.length];
};

const billingCycleOptions = Object.entries(billingCycleLabels).map(
    ([value, label]) => ({value, label})
);

const currencyOptions = Object.entries(currencyLabels).map(([value, label]) => ({
    value,
    label,
}));

const getTodayDate = (): string => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const getInitialValues = (
    subscription?: SubscriptionResponse,
    prefill?: PopularService
): FormValues => {
    if (subscription) {
        return {
            name: subscription.name,
            description: subscription.description ?? "",
            value: subscription.value.toString(),
            startDate: subscription.startDate,
            categoryId: subscription.categoryId?.toString() ?? "",
            paymentMethodId: subscription.paymentMethodId?.toString() ?? "",
            currency: subscription.currency,
            billingCycle: subscription.billingCycle,
            active: subscription.active,
            notifyUser: subscription.notifyUser,
            logoUrl: subscription.logoUrl ?? "",
        };
    }

    const today = getTodayDate();

    if (prefill) {
        return {
            name: prefill.name,
            description: "",
            value: prefill.defaultValue.toString(),
            startDate: today,
            categoryId: prefill.categoryId.toString(),
            paymentMethodId: "",
            currency: prefill.defaultCurrency,
            billingCycle: prefill.defaultBillingCycle,
            active: true,
            notifyUser: true,
            logoUrl: prefill.logoUrl,
        };
    }

    return {
        name: "",
        description: "",
        value: "",
        startDate: today,
        categoryId: "",
        paymentMethodId: "",
        currency: "BRL",
        billingCycle: "MONTHLY",
        active: true,
        notifyUser: true,
        logoUrl: "",
    };
};

export function SubscriptionForm({
                                     subscription,
                                     prefill,
                                     onSubmit,
                                     onCancel,
                                     onBack,
                                     isSubmitting = false,
                                 }: SubscriptionFormProps) {
    const [values, setValues] = useState<FormValues>(
        getInitialValues(subscription, prefill)
    );
    const [errors, setErrors] = useState<Partial<Record<keyof FormValues, string>>>({});

    const [editingAppearance, setEditingAppearance] = useState(false);
    const [customization, setCustomization] = useState({
        name: prefill?.name || subscription?.name || "",
        avatarColor: prefill?.brandColor || getDefaultColor(prefill?.name || subscription?.name || ""),
    });

    const isPopularService = !!prefill?.brandColor;
    const isEditing = !!subscription;

    // Sync form values when subscription or prefill changes
    useEffect(() => {
        setValues(getInitialValues(subscription, prefill));
        setCustomization({
            name: prefill?.name || subscription?.name || "",
            avatarColor: prefill?.brandColor || getDefaultColor(prefill?.name || subscription?.name || ""),
        });
    }, [subscription, prefill]);

    const validate = (): boolean => {
        const nextErrors: Partial<Record<keyof FormValues, string>> = {};

        // Name: required, max 255
        if (!values.name.trim()) {
            nextErrors.name = "Nome é obrigatório.";
        } else if (values.name.trim().length > 255) {
            nextErrors.name = "Nome deve ter no máximo 255 caracteres.";
        }

        // Description: optional, but if provided max 255
        if (values.description && values.description.trim().length > 255) {
            nextErrors.description = "Descrição deve ter no máximo 255 caracteres.";
        }

        // Value: required, must be positive
        const parsedValue = Number(values.value);
        if (!values.value || Number.isNaN(parsedValue)) {
            nextErrors.value = "Valor é obrigatório.";
        } else if (parsedValue <= 0) {
            nextErrors.value = "Valor deve ser maior que zero.";
        }

        // Start date: required
        if (!values.startDate) {
            nextErrors.startDate = "Data de início é obrigatória.";
        }

        // Currency: required
        if (!values.currency) {
            nextErrors.currency = "Moeda é obrigatória.";
        }

        // Category: required
        if (!values.categoryId) {
            nextErrors.categoryId = "Categoria é obrigatória.";
        }

        // Billing cycle: required
        if (!values.billingCycle) {
            nextErrors.billingCycle = "Ciclo de cobrança é obrigatório.";
        }

        // Payment method: required
        if (!values.paymentMethodId) {
            nextErrors.paymentMethodId = "Método de pagamento é obrigatório.";
        }

        // Logo URL: optional, but if provided max 255
        if (values.logoUrl && values.logoUrl.trim().length > 255) {
            nextErrors.logoUrl = "Logo URL deve ter no máximo 255 caracteres.";
        }

        setErrors(nextErrors);
        return Object.keys(nextErrors).length === 0;
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!validate()) {
            return;
        }

        const nextPaymentDate = calculateNextPaymentDate(
            values.startDate,
            values.billingCycle
        );

        await onSubmit({
            name: customization.name || values.name.trim(),
            description: values.description.trim() || undefined,
            value: Number(values.value),
            startDate: values.startDate,
            nextPaymentDate,
            currency: values.currency,
            billingCycle: values.billingCycle,
            active: values.active,
            notifyUser: values.notifyUser,
            logoUrl: values.logoUrl.trim() || undefined,
            categoryId: values.categoryId ? Number(values.categoryId) : undefined,
            paymentMethodId: values.paymentMethodId ? Number(values.paymentMethodId) : undefined,
            subscriptionTypeId: 1, // Always "Paid" by default
        });
    };

    const setValue = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
        setValues((prev) => ({...prev, [key]: value}));
    };

    const handleAppearanceSave = (name: string, color: string) => {
        setCustomization({name, avatarColor: color});
        setValue("name", name);
        setEditingAppearance(false);
    };

    return (
        <form className="space-y-4" onSubmit={handleSubmit}>
            {/* Back Button */}
            {onBack && (
                <button
                    type="button"
                    onClick={onBack}
                    className="flex items-center gap-2 text-sm font-medium text-neutral-600 hover:text-neutral-900 transition-colors -mb-2"
                >
                    <ArrowLeft className="w-4 h-4"/>
                    Voltar
                </button>
            )}

            {/* Subscription Header / Appearance Editor */}
            {(customization.name || values.name) && (
                editingAppearance ? (
                    <AppearanceEditor
                        name={customization.name || values.name}
                        logoUrl={values.logoUrl}
                        avatarColor={customization.avatarColor}
                        onSave={handleAppearanceSave}
                        onCancel={() => setEditingAppearance(false)}
                    />
                ) : (
                    <SubscriptionHeader
                        name={customization.name || values.name}
                        logoUrl={values.logoUrl}
                        avatarColor={customization.avatarColor}
                        onEdit={() => setEditingAppearance(true)}
                    />
                )
            )}

            {/* Form fields - hidden while editing appearance */}
            {!editingAppearance && (
                <>
                    {/* Nome */}
                    <Input
                        label="Nome"
                        value={values.name}
                        onChange={(event) => {
                            setValue("name", event.target.value);
                            if (!customization.name || customization.name === values.name) {
                                setCustomization((prev) => ({...prev, name: event.target.value}));
                            }
                        }}
                        error={errors.name}
                        maxLength={255}
                    />

                    {/* Descrição */}
                    <Input
                        label="Descrição"
                        value={values.description}
                        onChange={(event) => setValue("description", event.target.value)}
                        error={errors.description}
                        maxLength={255}
                    />

                    {/* Valor + Moeda */}
                    <div className="grid gap-4 sm:grid-cols-2">
                        <Input
                            label="Valor"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={values.value}
                            onChange={(event) => setValue("value", event.target.value)}
                            error={errors.value}
                        />

                        <Select
                            label="Moeda"
                            options={currencyOptions}
                            value={values.currency}
                            onChange={(event) => setValue("currency", event.target.value as Currency)}
                            error={errors.currency}
                        />
                    </div>

                    {/* Data de início + Categoria */}
                    <div className="grid gap-4 sm:grid-cols-2">
                        <Input
                            label="Data de início"
                            type="date"
                            value={values.startDate}
                            onChange={(event) => setValue("startDate", event.target.value)}
                            error={errors.startDate}
                        />

                        <Select
                            label="Categoria"
                            options={categoryOptions}
                            value={values.categoryId}
                            onChange={(event) => setValue("categoryId", event.target.value)}
                            placeholder="Selecione uma categoria"
                            error={errors.categoryId}
                        />
                    </div>

                    {/* Ciclo de cobrança + Método de Pagamento */}
                    <div className="grid gap-4 sm:grid-cols-2">
                        <Select
                            label="Ciclo de cobrança"
                            options={billingCycleOptions}
                            value={values.billingCycle}
                            onChange={(event) =>
                                setValue("billingCycle", event.target.value as BillingCycle)
                            }
                            error={errors.billingCycle}
                        />

                        <Select
                            label="Método de Pagamento"
                            options={paymentMethodOptions}
                            value={values.paymentMethodId}
                            onChange={(event) => setValue("paymentMethodId", event.target.value)}
                            placeholder="Selecione um método"
                            error={errors.paymentMethodId}
                        />
                    </div>

                    {/* Logo URL - only show for custom services */}
                    {!isPopularService && (
                        <Input
                            label="Logo URL"
                            type="url"
                            value={values.logoUrl}
                            onChange={(event) => setValue("logoUrl", event.target.value)}
                            error={errors.logoUrl}
                            maxLength={255}
                        />
                    )}

                    {/* Lembrete de renovação */}
                    <div
                        className="flex items-center justify-between rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3">
                        <div className="flex items-center gap-3">
                            <Bell className="w-4 h-4 text-neutral-500"/>
                            <span className="text-sm font-medium text-neutral-700">
                                Lembrete de renovação
                            </span>
                        </div>
                        <Toggle
                            checked={values.notifyUser}
                            onChange={(checked) => setValue("notifyUser", checked)}
                        />
                    </div>

                    {/* Action Buttons */}
                    <div className="flex justify-end gap-3 pt-2">
                        <Button type="button" variant="ghost" onClick={onCancel} disabled={isSubmitting}>
                            Cancelar
                        </Button>
                        <Button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? "Salvando..." : isEditing ? "Salvar alterações" : "Criar assinatura"}
                        </Button>
                    </div>
                </>
            )}
        </form>
    );
}
