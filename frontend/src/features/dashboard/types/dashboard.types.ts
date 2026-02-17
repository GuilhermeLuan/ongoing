/**
 * Dashboard Types
 * 
 * TypeScript interfaces mirroring backend DTOs for dashboard statistics.
 */

import { Currency } from '@/features/subscriptions';

/**
 * Total spending grouped by subscription category.
 * All values are in BRL.
 */
export interface CategorySpending {
  categoryId: number;
  categoryName: string;
  total: number;
}

/**
 * Consolidated dashboard statistics for authenticated user.
 * All monetary values are in BRL.
 */
export interface DashboardResponse {
  spendingByCategory: CategorySpending[];
  monthlyAverage: number;
  thisMonthTotal: number;
  yearlyTotal: number;
  currency: Currency;
  exchangeRateDate: string; // ISO date string from backend
}
