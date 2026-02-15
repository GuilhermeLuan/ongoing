# Lib

Utilities and mock data. Two files.

## utils.ts

Single utility function:

```typescript
import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
```

Used by every component for Tailwind class merging. Handles conditional classes, overrides, and deduplication.

## mock-data.ts

Mock data for the dashboard. Will be replaced by API calls when backend integration is complete.

### Types

```typescript
type Category = "VIDEO_STREAMING" | "MUSIC" | "GAMING" | "PRODUCTIVITY" | "CLOUD_STORAGE" | "NEWS" | "FITNESS" | "EDUCATION" | "OTHER";
type BillingCycle = "MONTHLY" | "YEARLY" | "WEEKLY";
type SubscriptionStatus = "ACTIVE" | "PAUSED" | "CANCELLED";

interface Subscription {
  id: number;
  name: string;
  category: Category;
  price: number;          // BRL
  billingCycle: BillingCycle;
  nextBilling: string;    // ISO date string "YYYY-MM-DD"
  status: SubscriptionStatus;
  logo?: string;
  color?: string;         // hex color for brand identification
}

interface DashboardStats {
  monthlySpending: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  expiringThisWeek: number;
  nextBillingDays: number;
  nextBillingName: string;
  categoryBreakdown: { category: Category; total: number }[];
}
```

### Label Maps

All labels are in Brazilian Portuguese:

| Export               | Type                                 | Purpose                                                                |
|----------------------|--------------------------------------|------------------------------------------------------------------------|
| `categoryLabels`     | `Record<Category, string>`           | e.g., "VIDEO_STREAMING" -> "Streaming de Video"                        |
| `categoryColors`     | `Record<Category, string>`           | Hex colors per category (e.g., VIDEO_STREAMING: "#E50914")             |
| `billingCycleLabels` | `Record<BillingCycle, string>`       | "MONTHLY" -> "Mensal", "YEARLY" -> "Anual", "WEEKLY" -> "Semanal"      |
| `statusLabels`       | `Record<SubscriptionStatus, string>` | "ACTIVE" -> "Ativa", "PAUSED" -> "Pausada", "CANCELLED" -> "Cancelada" |

### Mock Data

| Export              | Description                                                                                                                                                 |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `mockSubscriptions` | 12 subscriptions (Netflix, Spotify, Xbox Game Pass, Disney+, iCloud, ChatGPT Plus, Adobe CC, YouTube Premium, Amazon Prime, Notion, HBO Max, Duolingo Plus) |
| `mockStats`         | Pre-calculated dashboard stats (R$ 722.79/mo total, 12 total, 11 active)                                                                                    |

### Helper Functions

| Function              | Signature                                                  | Description                                                               |
|-----------------------|------------------------------------------------------------|---------------------------------------------------------------------------|
| `formatCurrency`      | `(value: number) => string`                                | Formats as BRL currency ("R$ 55,90") using `pt-BR` locale                 |
| `getUpcomingBillings` | `(subs: Subscription[], limit?: number) => Subscription[]` | Filters active, sorts by nextBilling ascending, returns top N (default 5) |
| `getDaysUntilBilling` | `(dateString: string) => number`                           | Days from today to billing date. Negative = past due                      |
| `formatDate`          | `(dateString: string) => string`                           | Formats as "15 de fev." style using `pt-BR` locale                        |
