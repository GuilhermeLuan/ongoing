/**
 * useDashboard Hook
 * 
 * Custom hook for fetching and managing dashboard statistics.
 */

'use client';

import { useState, useEffect } from 'react';
import { getDashboard } from '../services/dashboard.service';
import type { DashboardResponse } from '../types/dashboard.types';

interface UseDashboardReturn {
  dashboard: DashboardResponse | null;
  isLoading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
}

/**
 * Hook to fetch and manage dashboard statistics.
 * Auto-fetches on mount.
 * 
 * @returns Dashboard data, loading state, error, and refetch function
 */
export function useDashboard(): UseDashboardReturn {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchDashboard = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await getDashboard();
      setDashboard(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch dashboard'));
      console.error('Error fetching dashboard:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  return {
    dashboard,
    isLoading,
    error,
    refetch: fetchDashboard,
  };
}
