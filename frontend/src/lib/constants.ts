/**
 * Global Constants
 *
 * Centralized configuration for API endpoints, storage keys, and routes.
 */

// API Configuration
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6969/api/v1';

export const API_ENDPOINTS = {
    // Auth endpoints
    AUTH: {
        LOGIN: '/auth/login',
        REGISTER: '/auth/register',
        REFRESH: '/auth/refresh',
        // Note: Backend doesn't have a logout endpoint (tokens expire naturally)
    },

    // Resource endpoints
    SUBSCRIPTIONS: '/subscriptions',
    CATEGORIES: '/categories',
    DASHBOARD: '/dashboard',
} as const;

// LocalStorage Keys
export const STORAGE_KEYS = {
    REFRESH_TOKEN: 'ongoing_refresh_token',
} as const;

// Application Routes
export const ROUTES = {
    // Public routes
    HOME: '/',
    LOGIN: '/login',
    REGISTER: '/register',

    // Protected routes (app area)
    DASHBOARD: '/dashboard',
    SUBSCRIPTIONS: '/subscriptions',
    CATEGORIES: '/categories',
    CALENDAR: '/calendar',
    SETTINGS: '/settings',
} as const;
