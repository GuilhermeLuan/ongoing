/**
 * useAuth Hook Tests
 *
 * Unit tests for useAuth hook error handling and context consumption.
 */

import {describe, expect, it} from 'vitest';
import {renderHook} from '@testing-library/react';
import {useAuth} from './useAuth';
import {AuthProvider} from '../context/AuthContext';
import type {ReactNode} from 'react';

describe('useAuth', () => {
    describe('Error Handling', () => {
        it('should throw error when used outside AuthProvider', () => {
            // Suppress console.error for this test (React will log the error)
            const originalError = console.error;
            console.error = () => {
            };

            expect(() => {
                renderHook(() => useAuth());
            }).toThrow('useAuth must be used within an AuthProvider');

            console.error = originalError;
        });
    });

    describe('Context Consumption', () => {
        it('should return auth context when used within AuthProvider', () => {
            const wrapper = ({children}: { children: ReactNode }) => (
                <AuthProvider>{children}</AuthProvider>
            );

            const {result} = renderHook(() => useAuth(), {wrapper});

            // Verify the hook returns the expected context value
            expect(result.current).toBeDefined();
            expect(result.current.user).toBeNull(); // Initial state
            expect(result.current.accessToken).toBeNull(); // Initial state
            expect(result.current.isLoading).toBe(false);
            expect(result.current.error).toBeNull();
            expect(typeof result.current.login).toBe('function');
            expect(typeof result.current.register).toBe('function');
            expect(typeof result.current.logout).toBe('function');
            expect(typeof result.current.refreshAuth).toBe('function');
            expect(typeof result.current.clearError).toBe('function');
        });

        it('should have isInitialized set to true after mount', async () => {
            const wrapper = ({children}: { children: ReactNode }) => (
                <AuthProvider>{children}</AuthProvider>
            );

            const {result} = renderHook(() => useAuth(), {wrapper});

            // Wait for initialization to complete
            await new Promise((resolve) => setTimeout(resolve, 100));

            // After mount, isInitialized should be true
            expect(result.current.isInitialized).toBe(true);
        });
    });

    describe('State Properties', () => {
        it('should expose all required state properties', () => {
            const wrapper = ({children}: { children: ReactNode }) => (
                <AuthProvider>{children}</AuthProvider>
            );

            const {result} = renderHook(() => useAuth(), {wrapper});

            // Verify all state properties exist
            expect(result.current).toHaveProperty('user');
            expect(result.current).toHaveProperty('accessToken');
            expect(result.current).toHaveProperty('isLoading');
            expect(result.current).toHaveProperty('error');
            expect(result.current).toHaveProperty('isInitialized');
        });
    });

    describe('Method Properties', () => {
        it('should expose all required methods', () => {
            const wrapper = ({children}: { children: ReactNode }) => (
                <AuthProvider>{children}</AuthProvider>
            );

            const {result} = renderHook(() => useAuth(), {wrapper});

            // Verify all methods exist
            expect(result.current).toHaveProperty('login');
            expect(result.current).toHaveProperty('register');
            expect(result.current).toHaveProperty('logout');
            expect(result.current).toHaveProperty('refreshAuth');
            expect(result.current).toHaveProperty('clearError');
        });
    });

    describe('Type Safety', () => {
        it('should maintain type consistency', () => {
            const wrapper = ({children}: { children: ReactNode }) => (
                <AuthProvider>{children}</AuthProvider>
            );

            const {result} = renderHook(() => useAuth(), {wrapper});

            // TypeScript should enforce these types at compile time
            // At runtime, we verify the structure matches expectations
            expect(result.current.user).toBeNull(); // User | null
            expect(result.current.accessToken).toBeNull(); // string | null
            expect(typeof result.current.isLoading).toBe('boolean');
            expect(result.current.error).toBeNull(); // string | null
            expect(typeof result.current.isInitialized).toBe('boolean');
        });
    });
});
