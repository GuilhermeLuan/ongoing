/**
 * Token Storage Utility
 *
 * Abstraction layer for storing refresh tokens in localStorage.
 * Includes SSR guards to prevent accessing localStorage during server-side rendering.
 *
 * NOTE: This file will be DELETED when migrating to HttpOnly cookies.
 * The hybrid approach (localStorage) is temporary and less secure than cookies.
 */

import {STORAGE_KEYS} from '@/lib/constants';

/**
 * Check if localStorage is available (client-side only)
 */
const isLocalStorageAvailable = (): boolean => {
    return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
};

/**
 * Token Storage Operations
 */
export const tokenStorage = {
    /**
     * Get refresh token from localStorage
     * @returns Refresh token or null if not found
     */
    getRefreshToken(): string | null {
        if (!isLocalStorageAvailable()) {
            return null;
        }

        try {
            return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
        } catch (error) {
            console.error('Error reading refresh token from localStorage:', error);
            return null;
        }
    },

    /**
     * Save refresh token to localStorage
     * @param token - Refresh token to store
     */
    setRefreshToken(token: string): void {
        if (!isLocalStorageAvailable()) {
            console.warn('localStorage not available, cannot save refresh token');
            return;
        }

        try {
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, token);
        } catch (error) {
            console.error('Error saving refresh token to localStorage:', error);
        }
    },

    /**
     * Remove refresh token from localStorage
     */
    removeRefreshToken(): void {
        if (!isLocalStorageAvailable()) {
            return;
        }

        try {
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        } catch (error) {
            console.error('Error removing refresh token from localStorage:', error);
        }
    },

    /**
     * Check if refresh token exists in localStorage
     * @returns True if refresh token exists
     */
    hasRefreshToken(): boolean {
        return this.getRefreshToken() !== null;
    },

    /**
     * Clear all auth-related data from localStorage
     * (For future use if we store additional auth data)
     */
    clearAll(): void {
        this.removeRefreshToken();
    },
};
