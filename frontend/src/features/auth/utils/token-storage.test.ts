/**
 * Token Storage Tests
 *
 * Unit tests for localStorage abstraction and SSR guards.
 */

import {beforeEach, describe, expect, it, vi} from 'vitest';
import {tokenStorage} from './token-storage';
import {STORAGE_KEYS} from '@/lib/constants';

describe('tokenStorage', () => {
    beforeEach(() => {
        // Clear localStorage before each test
        localStorage.clear();
    });

    describe('getRefreshToken', () => {
        it('should return null when no token is stored', () => {
            const token = tokenStorage.getRefreshToken();
            expect(token).toBeNull();
        });

        it('should return stored refresh token', () => {
            const mockToken = 'test-refresh-token-123';
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, mockToken);

            const token = tokenStorage.getRefreshToken();
            expect(token).toBe(mockToken);
        });

        it('should handle localStorage errors gracefully', () => {
            // Mock localStorage.getItem to throw an error
            const getItemSpy = vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
                throw new Error('localStorage error');
            });

            const token = tokenStorage.getRefreshToken();

            expect(token).toBeNull();

            getItemSpy.mockRestore();
        });
    });

    describe('setRefreshToken', () => {
        it('should save refresh token to localStorage', () => {
            const mockToken = 'test-refresh-token-456';

            tokenStorage.setRefreshToken(mockToken);

            const storedToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
            expect(storedToken).toBe(mockToken);
        });

        it('should handle localStorage errors gracefully', () => {
            const setItemSpy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
                throw new Error('localStorage quota exceeded');
            });

            // Should not throw when error occurs
            expect(() => tokenStorage.setRefreshToken('test-token')).not.toThrow();

            setItemSpy.mockRestore();
        });
    });

    describe('removeRefreshToken', () => {
        it('should remove refresh token from localStorage', () => {
            // First, set a token
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, 'test-token');
            expect(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)).toBe('test-token');

            // Then remove it
            tokenStorage.removeRefreshToken();

            expect(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)).toBeNull();
        });

        it('should handle localStorage errors gracefully', () => {
            const removeItemSpy = vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
                throw new Error('localStorage error');
            });

            // Should not throw when error occurs
            expect(() => tokenStorage.removeRefreshToken()).not.toThrow();

            removeItemSpy.mockRestore();
        });
    });

    describe('hasRefreshToken', () => {
        it('should return false when no token is stored', () => {
            expect(tokenStorage.hasRefreshToken()).toBe(false);
        });

        it('should return true when token is stored', () => {
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, 'test-token');
            expect(tokenStorage.hasRefreshToken()).toBe(true);
        });
    });

    describe('clearAll', () => {
        it('should remove all auth-related data', () => {
            // Set a refresh token
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, 'test-token');
            expect(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)).toBe('test-token');

            // Clear all
            tokenStorage.clearAll();

            expect(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)).toBeNull();
        });
    });

    describe('SSR guards', () => {
        it('should handle SSR environment (no window)', () => {
            // This test runs in jsdom which has window, but we test the logic
            // The actual SSR guard is: typeof window !== 'undefined'
            // In a real SSR environment, these methods would return early

            // For now, we just verify the methods don't throw errors
            expect(() => tokenStorage.getRefreshToken()).not.toThrow();
            expect(() => tokenStorage.setRefreshToken('token')).not.toThrow();
            expect(() => tokenStorage.removeRefreshToken()).not.toThrow();
            expect(() => tokenStorage.hasRefreshToken()).not.toThrow();
            expect(() => tokenStorage.clearAll()).not.toThrow();
        });
    });
});
