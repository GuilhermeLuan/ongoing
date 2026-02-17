import type {BillingCycle, CategoryOption, Currency, PaymentMethodOption} from "../types/subscription.types";

export const billingCycleLabels: Record<BillingCycle, string> = {
    MONTHLY: "Mensal",
    QUARTERLY: "Trimestral",
    SEMI_ANNUAL: "Semestral",
    YEARLY: "Anual",
    WEEKLY: "Semanal",
    BIWEEKLY: "Quinzenal",
};

export const currencyLabels: Record<Currency, string> = {
    BRL: "Real (BRL)",
    USD: "Dólar (USD)",
    EUR: "Euro (EUR)",
};

/**
 * Category options matching the backend seed data (V1.1__insert_domains_value.sql).
 * These are hardcoded as there's no backend endpoint for categories.
 */
export const CATEGORIES: CategoryOption[] = [
    {id: 1, name: "Video Streaming"},
    {id: 2, name: "Music Streaming"},
    {id: 3, name: "Gaming"},
    {id: 4, name: "Software / SaaS"},
    {id: 5, name: "Education"},
    {id: 6, name: "Health & Fitness"},
    {id: 7, name: "Utilities"},
    {id: 8, name: "Insurance"},
    {id: 9, name: "Other"},
];

/**
 * Category options formatted for Select component usage.
 */
export const categoryOptions = CATEGORIES.map(({id, name}) => ({
    value: id.toString(),
    label: name,
}));

/**
 * Payment method options matching the backend seed data (V1.1__insert_domains_value.sql).
 * These are hardcoded as there's no backend endpoint for payment methods.
 */
export const PAYMENT_METHODS: PaymentMethodOption[] = [
    {id: 1, name: "Cartão de Crédito"},
    {id: 2, name: "Cartão de Débito"},
    {id: 3, name: "PIX"},
    {id: 4, name: "Boleto"},
    {id: 5, name: "PayPal"},
    {id: 6, name: "Débito Direto"},
];

/**
 * Payment method options formatted for Select component usage.
 */
export const paymentMethodOptions = PAYMENT_METHODS.map(({id, name}) => ({
    value: id.toString(),
    label: name,
}));

const currencyLocaleMap: Record<Currency, string> = {
    BRL: "pt-BR",
    USD: "en-US",
    EUR: "de-DE",
};

export const formatSubscriptionValue = (
    value: number,
    currency: Currency = "BRL"
): string => {
    return value.toLocaleString(currencyLocaleMap[currency], {
        style: "currency",
        currency,
    });
};

export const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString("pt-BR", {
        day: "2-digit",
        month: "short",
    });
};

export const getDaysUntilBilling = (dateString: string): number => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const billingDate = new Date(dateString);
    billingDate.setHours(0, 0, 0, 0);

    const diffTime = billingDate.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

/**
 * Calculates the next payment date based on start date and billing cycle.
 * Mirrors the backend logic (SubscriptionsService.calculateNextBillingDate).
 * Handles end-of-month edge cases (e.g., Jan 31 + 1 month = Feb 28/29).
 *
 * @param startDate - ISO date string (YYYY-MM-DD)
 * @param billingCycle - Billing cycle type
 * @returns ISO date string (YYYY-MM-DD) for next payment
 */
export const calculateNextPaymentDate = (
    startDate: string,
    billingCycle: BillingCycle
): string => {
    const date = new Date(startDate);

    switch (billingCycle) {
        case "WEEKLY":
            date.setDate(date.getDate() + 7);
            break;
        case "BIWEEKLY":
            date.setDate(date.getDate() + 14);
            break;
        case "MONTHLY":
            date.setMonth(date.getMonth() + 1);
            break;
        case "QUARTERLY":
            date.setMonth(date.getMonth() + 3);
            break;
        case "SEMI_ANNUAL":
            date.setMonth(date.getMonth() + 6);
            break;
        case "YEARLY":
            date.setFullYear(date.getFullYear() + 1);
            break;
    }

    // Convert to YYYY-MM-DD format
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
};

/**
 * Get category name from subscription.
 * Prefers categoryName from backend, falls back to ID lookup for backwards compatibility.
 */
export const getCategoryName = (subscription: { categoryId: number | null; categoryName?: string | null }): string => {
    // Prefer backend-provided name (source of truth)
    if (subscription.categoryName) return subscription.categoryName;

    // Fallback to ID lookup (backwards compatibility)
    if (!subscription.categoryId) return "Sem categoria";
    const category = categoryOptions.find(c => c.value === subscription.categoryId?.toString());
    return category?.label ?? "Sem categoria";
};

/**
 * Get payment method name from subscription.
 * Prefers paymentMethodName from backend, falls back to ID lookup for backwards compatibility.
 */
export const getPaymentMethodName = (subscription: {
    paymentMethodId: number | null;
    paymentMethodName?: string | null
}): string => {
    // Prefer backend-provided name (source of truth)
    if (subscription.paymentMethodName) return subscription.paymentMethodName;

    // Fallback to ID lookup (backwards compatibility)
    if (!subscription.paymentMethodId) return "Não informado";
    const method = paymentMethodOptions.find(m => m.value === subscription.paymentMethodId?.toString());
    return method?.label ?? "Não informado";
};
