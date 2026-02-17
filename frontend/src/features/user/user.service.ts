/**
 * User Service
 *
 * API methods for user profile operations.
 */

import { apiClient } from '@/features/auth/services/api-client';

export interface UpdateUserRequest {
    name?: string;
    onboardingCompleted?: boolean;
}

export interface UserResponse {
    id: number;
    name: string;
    email: string;
    onboardingCompleted: boolean;
}

/**
 * Get current user profile
 */
export const getCurrentUser = async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>('/users/me');
    return response.data;
};

/**
 * Update current user profile
 */
export const updateCurrentUser = async (
    data: UpdateUserRequest
): Promise<UserResponse> => {
    const response = await apiClient.patch<UserResponse>('/users/me', data);
    return response.data;
};

/**
 * Export user service
 */
export const userService = {
    getCurrentUser,
    updateCurrentUser,
};
