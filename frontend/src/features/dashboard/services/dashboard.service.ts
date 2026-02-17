/**
 * Dashboard Service
 * 
 * Service for fetching dashboard statistics from the backend API.
 */

import { apiClient } from '@/features/auth';
import { API_ENDPOINTS } from '@/lib/constants';
import type { DashboardResponse } from '../types/dashboard.types';

/**
 * Fetches dashboard statistics for the authenticated user.
 * 
 * @returns Promise resolving to dashboard data with spending statistics
 * @throws Error if the request fails
 */
export async function getDashboard(): Promise<DashboardResponse> {
  const response = await apiClient.get<DashboardResponse>(API_ENDPOINTS.DASHBOARD);
  return response.data;
}
