import type {SubscriptionResponse} from "@/features/subscriptions/types/subscription.types";

export type Category =
  | "VIDEO_STREAMING"
  | "MUSIC"
  | "GAMING"
  | "PRODUCTIVITY"
  | "CLOUD_STORAGE"
  | "NEWS"
  | "FITNESS"
  | "EDUCATION"
  | "OTHER";

export type BillingCycle = "MONTHLY" | "YEARLY" | "WEEKLY";

export type SubscriptionStatus = "ACTIVE" | "PAUSED" | "CANCELLED";

export interface Subscription {
  id: number;
  name: string;
  category: Category;
  price: number;
  billingCycle: BillingCycle;
  nextBilling: string;
  status: SubscriptionStatus;
  logo?: string;
  color?: string;
}

export const categoryLabels: Record<Category, string> = {
  VIDEO_STREAMING: "Streaming de Vídeo",
  MUSIC: "Música",
  GAMING: "Jogos",
  PRODUCTIVITY: "Produtividade",
  CLOUD_STORAGE: "Armazenamento",
  NEWS: "Notícias",
  FITNESS: "Fitness",
  EDUCATION: "Educação",
  OTHER: "Outros",
};

export const categoryColors: Record<Category, string> = {
  VIDEO_STREAMING: "#E50914",
  MUSIC: "#1DB954",
  GAMING: "#5865F2",
  PRODUCTIVITY: "#0078D4",
  CLOUD_STORAGE: "#4285F4",
  NEWS: "#FF6600",
  FITNESS: "#FC4C02",
  EDUCATION: "#FF9900",
  OTHER: "#6B7280",
};

export const billingCycleLabels: Record<BillingCycle, string> = {
  MONTHLY: "Mensal",
  YEARLY: "Anual",
  WEEKLY: "Semanal",
};

export const statusLabels: Record<SubscriptionStatus, string> = {
  ACTIVE: "Ativa",
  PAUSED: "Pausada",
  CANCELLED: "Cancelada",
};

export const mockSubscriptions: Subscription[] = [
  {
    id: 1,
    name: "Netflix",
    category: "VIDEO_STREAMING",
    price: 55.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-15",
    status: "ACTIVE",
    color: "#E50914",
  },
  {
    id: 2,
    name: "Spotify",
    category: "MUSIC",
    price: 21.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-10",
    status: "ACTIVE",
    color: "#1DB954",
  },
  {
    id: 3,
    name: "Xbox Game Pass",
    category: "GAMING",
    price: 44.99,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-20",
    status: "ACTIVE",
    color: "#107C10",
  },
  {
    id: 4,
    name: "Disney+",
    category: "VIDEO_STREAMING",
    price: 33.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-18",
    status: "ACTIVE",
    color: "#113CCF",
  },
  {
    id: 5,
    name: "iCloud",
    category: "CLOUD_STORAGE",
    price: 3.5,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-12",
    status: "ACTIVE",
    color: "#3693F3",
  },
  {
    id: 6,
    name: "ChatGPT Plus",
    category: "PRODUCTIVITY",
    price: 100.0,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-25",
    status: "ACTIVE",
    color: "#10A37F",
  },
  {
    id: 7,
    name: "Adobe Creative Cloud",
    category: "PRODUCTIVITY",
    price: 290.0,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-28",
    status: "ACTIVE",
    color: "#FF0000",
  },
  {
    id: 8,
    name: "YouTube Premium",
    category: "VIDEO_STREAMING",
    price: 24.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-08",
    status: "ACTIVE",
    color: "#FF0000",
  },
  {
    id: 9,
    name: "Amazon Prime",
    category: "VIDEO_STREAMING",
    price: 14.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-22",
    status: "ACTIVE",
    color: "#FF9900",
  },
  {
    id: 10,
    name: "Notion",
    category: "PRODUCTIVITY",
    price: 48.0,
    billingCycle: "YEARLY",
    nextBilling: "2024-08-15",
    status: "ACTIVE",
    color: "#000000",
  },
  {
    id: 11,
    name: "HBO Max",
    category: "VIDEO_STREAMING",
    price: 34.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-14",
    status: "PAUSED",
    color: "#5822B4",
  },
  {
    id: 12,
    name: "Duolingo Plus",
    category: "EDUCATION",
    price: 49.9,
    billingCycle: "MONTHLY",
    nextBilling: "2024-02-05",
    status: "ACTIVE",
    color: "#58CC02",
  },
];

export interface DashboardStats {
  monthlySpending: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  expiringThisWeek: number;
  nextBillingDays: number;
  nextBillingName: string;
  categoryBreakdown: { category: Category; total: number }[];
}

export const mockStats: DashboardStats = {
  monthlySpending: 722.79,
  totalSubscriptions: 12,
  activeSubscriptions: 11,
  expiringThisWeek: 3,
  nextBillingDays: 2,
  nextBillingName: "Duolingo Plus",
  categoryBreakdown: [
    { category: "VIDEO_STREAMING", total: 164.5 },
    { category: "PRODUCTIVITY", total: 438.0 },
    { category: "MUSIC", total: 21.9 },
    { category: "GAMING", total: 44.99 },
    { category: "CLOUD_STORAGE", total: 3.5 },
    { category: "EDUCATION", total: 49.9 },
  ],
};

export function formatCurrency(value: number): string {
  return value.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

export function getUpcomingBillings(
  subscriptions: Subscription[],
  limit: number = 5
): Subscription[] {
  return subscriptions
    .filter((sub) => sub.status === "ACTIVE")
    .sort(
      (a, b) =>
        new Date(a.nextBilling).getTime() - new Date(b.nextBilling).getTime()
    )
    .slice(0, limit);
}

export function getDaysUntilBilling(dateString: string): number {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const billingDate = new Date(dateString);
  billingDate.setHours(0, 0, 0, 0);
  const diffTime = billingDate.getTime() - today.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

export function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
  });
}

export function toSubscriptionResponse(
    subscription: Subscription
): SubscriptionResponse {
    return {
        id: subscription.id,
        name: subscription.name,
        description: categoryLabels[subscription.category],
        value: subscription.price,
        startDate: subscription.nextBilling,
        nextPaymentDate: subscription.nextBilling,
        active: subscription.status === "ACTIVE",
        notifyUser: true,
        currency: "BRL",
        logoUrl: subscription.logo ?? null,
        categoryId: null,
        paymentMethodId: null,
        billingCycle: subscription.billingCycle,
        subscriptionTypeId: null,
        categoryName: null,
        paymentMethodName: null,
    };
}
