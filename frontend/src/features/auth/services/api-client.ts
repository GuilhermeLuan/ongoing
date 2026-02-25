/**
 * API Client
 *
 * Axios HTTP client with request/response interceptors for authentication.
 *
 * Features:
 * - Automatic token injection (Authorization header)
 * - 401 handling with automatic token refresh
 * - Request queuing during token refresh
 * - Callback pattern to avoid circular dependency with AuthContext
 */

import axios, {AxiosError, AxiosInstance, InternalAxiosRequestConfig} from 'axios';
import {API_BASE_URL, API_ENDPOINTS} from '@/lib/constants';
import type {AuthCallbacks} from '../types/auth.types';

/**
 * Auth callbacks registered by AuthContext
 * This avoids circular dependency: api-client ↔ AuthContext
 */
let authCallbacks: AuthCallbacks | null = null;

const AUTH_ENDPOINTS = [
    API_ENDPOINTS.AUTH.LOGIN,
    API_ENDPOINTS.AUTH.REGISTER,
    API_ENDPOINTS.AUTH.REFRESH,
];

/**
 * Register auth callbacks for use by interceptors
 * Called by AuthContext during initialization
 */
export const registerAuthCallbacks = (callbacks: AuthCallbacks): void => {
    authCallbacks = callbacks;
};

/**
 * Flag to prevent infinite refresh loops
 */
let isRefreshing = false;

/**
 * Queue of failed requests waiting for token refresh
 */
let failedRequestsQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (error?: unknown) => void;
}> = [];

/**
 * Process queued requests after successful refresh
 */
const processQueue = (error: AxiosError | null = null): void => {
    failedRequestsQueue.forEach((promise) => {
        if (error) {
            promise.reject(error);
        } else {
            promise.resolve();
        }
    });

    failedRequestsQueue = [];
};

/**
 * Axios Instance
 */
export const apiClient: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000, // 10 seconds
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Request Interceptor
 * Injects Authorization header with access token
 */
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // Skip token injection for auth endpoints (login, register, refresh)
        const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) =>
            config.url?.includes(endpoint)
        );

        if (isAuthEndpoint) {
            return config;
        }

        // Get access token from callback (if registered)
        const accessToken = authCallbacks?.getAccessToken();

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/**
 * Response Interceptor
 * Handles 401 Unauthorized responses with automatic token refresh
 */
apiClient.interceptors.response.use(
    (response) => {
        // Success response, pass through
        return response;
    },
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
            _retry?: boolean;
        };
        const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) =>
            originalRequest.url?.includes(endpoint)
        );

        // Handle 401 Unauthorized
        if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
            // Prevent infinite loops
            originalRequest._retry = true;

            if (!authCallbacks) {
                console.error('Auth callbacks not registered');
                return Promise.reject(error);
            }

            // If refresh is already in progress, queue this request
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedRequestsQueue.push({resolve, reject});
                })
                    .then(() => {
                        // Retry request with new token
                        return apiClient(originalRequest);
                    })
                    .catch((err) => {
                        return Promise.reject(err);
                    });
            }

            // Start refresh process
            isRefreshing = true;

            try {
                // Attempt to refresh token
                const refreshSuccess = await authCallbacks.refreshToken();

                if (refreshSuccess) {
                    // Refresh succeeded, process queued requests
                    processQueue();
                    isRefreshing = false;

                    // Retry original request with new token
                    return apiClient(originalRequest);
                } else {
                    // Refresh failed, logout user
                    processQueue(error);
                    isRefreshing = false;
                    authCallbacks.logout();
                    return Promise.reject(error);
                }
            } catch (refreshError) {
                // Refresh failed, logout user
                processQueue(error);
                isRefreshing = false;
                authCallbacks.logout();
                return Promise.reject(refreshError);
            }
        }

        // Not a 401 or already retried, reject
        return Promise.reject(error);
    }
);

/**
 * Export axios instance as default
 */
export default apiClient;
