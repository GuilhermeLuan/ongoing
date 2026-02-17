export type BillingCycle =
    | "MONTHLY"
    | "QUARTERLY"
    | "SEMI_ANNUAL"
    | "YEARLY"
    | "WEEKLY"
    | "BIWEEKLY";

export type Currency = "BRL" | "USD" | "EUR";

export interface SubscriptionResponse {
    id: number;
    name: string;
    description: string | null;
    value: number;
    startDate: string;
    nextPaymentDate: string;
    active: boolean;
    notifyUser: boolean;
    currency: Currency;
    logoUrl: string | null;
    categoryId: number | null;
    paymentMethodId: number | null;
    billingCycle: BillingCycle;
    subscriptionTypeId: number | null;
    categoryName: string | null;
    paymentMethodName: string | null;
}

export interface SubscriptionRequest {
    name: string;
    description?: string;
    value: number;
    startDate: string;
    nextPaymentDate: string;
    active?: boolean;
    notifyUser?: boolean;
    currency?: Currency;
    logoUrl?: string;
    categoryId?: number;
    paymentMethodId?: number;
    billingCycle: BillingCycle;
    subscriptionTypeId?: number;
}

export interface SpringPage<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
}

export interface SubscriptionFilters {
    name?: string;
    active?: boolean;
    categoryId?: number;
    page?: number;
    size?: number;
}

export interface PopularService {
    name: string;
    logoUrl: string;
    categoryId: number;
    defaultBillingCycle: BillingCycle;
    defaultValue: number;
    defaultCurrency: Currency;
    brandColor?: string;
}

export interface CategoryOption {
    id: number;
    name: string;
}

export interface PaymentMethodOption {
    id: number;
    name: string;
}
