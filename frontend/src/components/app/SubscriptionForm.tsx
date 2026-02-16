"use client";

import {useState} from "react";
import {ArrowLeft} from "lucide-react";
import {Button, Input, Select} from "@/components/ui";
import {
    type BillingCycle,
    billingCycleLabels,
    calculateNextPaymentDate,
    categoryOptions,
    type Currency,
    currencyLabels,
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
    currency: Currency;
    billingCycle: BillingCycle;
    active: boolean;
    notifyUser: boolean;
    logoUrl: string;
}

const billingCycleOptions = Object.entries(billingCycleLabels).map(
    ([value, label]) => ({value, label})
);

const currencyOptions = Object.entries(currencyLabels).map(([value, label]) => ({
    value,
    label,
}));

const getInitialValues = (
    subscription?: SubscriptionResponse,
    prefill?: PopularService
): FormValues => {
    // If editing existing subscription
    if (subscription) {
        return {
            name: subscription.name,
            description: subscription.description ?? "",
            value: subscription.value.toString(),
            startDate: subscription.startDate,
            categoryId: subscription.categoryId?.toString() ?? "",
            currency: subscription.currency,
            billingCycle: subscription.billingCycle,
            active: subscription.active,
            notifyUser: subscription.notifyUser,
            logoUrl: subscription.logoUrl ?? "",
        };
    }

    // If pre-filling from popular service
    if (prefill) {
        return {
            name: prefill.name,
            description: "",
            value: prefill.defaultValue.toString(),
            startDate: "",
            categoryId: prefill.categoryId.toString(),
            currency: prefill.defaultCurrency,
            billingCycle: prefill.defaultBillingCycle,
            active: true,
            notifyUser: true,
            logoUrl: prefill.logoUrl,
        };
    }

    // Default empty form
    return {
        name: "",
        description: "",
        value: "",
        startDate: "",
        categoryId: "",
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

    const validate = (): boolean => {
        const nextErrors: Partial<Record<keyof FormValues, string>> = {};

        if (!values.name.trim()) {
            nextErrors.name = "Nome é obrigatório.";
        } else if (values.name.trim().length > 255) {
            nextErrors.name = "Nome deve ter no máximo 255 caracteres.";
        }

        if (values.description.trim().length > 255) {
            nextErrors.description = "Descrição deve ter no máximo 255 caracteres.";
        }

        const parsedValue = Number(values.value);
        if (!values.value || Number.isNaN(parsedValue)) {
            nextErrors.value = "Valor é obrigatório.";
        } else if (parsedValue <= 0) {
            nextErrors.value = "Valor deve ser positivo.";
        }

        if (!values.startDate) {
            nextErrors.startDate = "Data de início é obrigatória.";
        }

        if (!values.billingCycle) {
            nextErrors.billingCycle = "Ciclo de cobrança é obrigatório.";
        }

        if (values.logoUrl.length > 255) {
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

        // Calculate nextPaymentDate from startDate and billingCycle
        // Backend recalculates this anyway, but it satisfies the @NotNull DTO requirement
        const nextPaymentDate = calculateNextPaymentDate(
            values.startDate,
            values.billingCycle
        );

        await onSubmit({
            name: values.name.trim(),
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
        });
    };

    const setValue = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
        setValues((prev) => ({...prev, [key]: value}));
    };

    return (
        <form className="space-y-4" onSubmit={handleSubmit}>
            {/* Back Button (only shown when onBack is provided) */}
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

            {/* Nome */}
            <Input
                label="Nome"
                value={values.name}
                onChange={(event) => setValue("name", event.target.value)}
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
                />
            </div>

            {/* Ciclo de cobrança */}
            <Select
                label="Ciclo de cobrança"
                options={billingCycleOptions}
                value={values.billingCycle}
                onChange={(event) =>
                    setValue("billingCycle", event.target.value as BillingCycle)
                }
                error={errors.billingCycle}
            />

            {/* Logo URL */}
            <Input
                label="Logo URL"
                type="url"
                value={values.logoUrl}
                onChange={(event) => setValue("logoUrl", event.target.value)}
                error={errors.logoUrl}
                maxLength={255}
            />

            {/* Checkboxes: Ativo + Notificar */}
            <div className="grid gap-3 sm:grid-cols-2">
                <label className="flex items-center gap-2 text-sm text-neutral-700">
                    <input
                        type="checkbox"
                        checked={values.active}
                        onChange={(event) => setValue("active", event.target.checked)}
                        className="h-4 w-4 rounded border-neutral-300 text-primary-600 focus:ring-primary-500"
                    />
                    Assinatura ativa
                </label>

                <label className="flex items-center gap-2 text-sm text-neutral-700">
                    <input
                        type="checkbox"
                        checked={values.notifyUser}
                        onChange={(event) => setValue("notifyUser", event.target.checked)}
                        className="h-4 w-4 rounded border-neutral-300 text-primary-600 focus:ring-primary-500"
                    />
                    Notificar usuário
                </label>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="ghost" onClick={onCancel} disabled={isSubmitting}>
                    Cancelar
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "Salvando..." : subscription ? "Salvar alterações" : "Criar assinatura"}
                </Button>
            </div>
        </form>
    );
}
