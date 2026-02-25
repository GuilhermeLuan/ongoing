/**
 * API Client Tests
 *
 * Unit tests for callback registration and auth logic.
 */

import {beforeEach, describe, expect, it, vi} from 'vitest';
import {apiClient, registerAuthCallbacks} from './api-client';
import {API_ENDPOINTS} from '@/lib/constants';
import type {AuthCallbacks} from '../types/auth.types';

const getResponseRejectedInterceptor = () => {
    const handlers = (apiClient.interceptors.response as unknown as {
        handlers: Array<{ rejected?: (error: unknown) => Promise<unknown> }>;
    }).handlers;

    const rejected = handlers[0]?.rejected;

    if (!rejected) {
        throw new Error('Response rejected interceptor not found');
    }

    return rejected;
};

describe('apiClient', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('registerAuthCallbacks', () => {
        it('should register auth callbacks successfully', () => {
            const callbacks: AuthCallbacks = {
                getAccessToken: vi.fn(() => 'test-token'),
                refreshToken: vi.fn(async () => true),
                logout: vi.fn(),
            };

            expect(() => registerAuthCallbacks(callbacks)).not.toThrow();
        });

        it('should allow updating callbacks', () => {
            const callbacks1: AuthCallbacks = {
                getAccessToken: vi.fn(() => 'token-1'),
                refreshToken: vi.fn(async () => true),
                logout: vi.fn(),
            };

            const callbacks2: AuthCallbacks = {
                getAccessToken: vi.fn(() => 'token-2'),
                refreshToken: vi.fn(async () => true),
                logout: vi.fn(),
            };

            registerAuthCallbacks(callbacks1);
            registerAuthCallbacks(callbacks2); // Should override

            expect(callbacks2.getAccessToken()).toBe('token-2');
        });
    });

    describe('Configuration', () => {
        it('should have correct base configuration', () => {
            expect(apiClient).toBeDefined();
            expect(apiClient.defaults.timeout).toBe(10000);
            expect(apiClient.defaults.headers['Content-Type']).toBe('application/json');
        });

        it('should have baseURL configured', () => {
            expect(apiClient.defaults.baseURL).toBeDefined();
        });
    });

    describe('Auth Endpoints', () => {
        it('should have correct auth endpoint paths', () => {
            expect(API_ENDPOINTS.AUTH.LOGIN).toBe('/auth/login');
            expect(API_ENDPOINTS.AUTH.REGISTER).toBe('/auth/register');
            expect(API_ENDPOINTS.AUTH.REFRESH).toBe('/auth/refresh');
        });
    });

    describe('Interceptor Logic', () => {
        it('should have request interceptor configured', () => {
            expect(apiClient.interceptors.request).toBeDefined();
        });

        it('should have response interceptor configured', () => {
            expect(apiClient.interceptors.response).toBeDefined();
        });

        it('should not trigger refresh flow for 401 from /auth/refresh', async () => {
            const callbacks: AuthCallbacks = {
                getAccessToken: vi.fn(() => null),
                refreshToken: vi.fn(async () => false),
                logout: vi.fn(),
            };
            registerAuthCallbacks(callbacks);

            const rejected = getResponseRejectedInterceptor();
            const refreshEndpointError = {
                config: {url: API_ENDPOINTS.AUTH.REFRESH, headers: {}},
                response: {status: 401},
            };

            await expect(rejected(refreshEndpointError)).rejects.toBe(refreshEndpointError);
            expect(callbacks.refreshToken).not.toHaveBeenCalled();
            expect(callbacks.logout).not.toHaveBeenCalled();
        });

        it('should trigger refresh flow for 401 from protected endpoints', async () => {
            const callbacks: AuthCallbacks = {
                getAccessToken: vi.fn(() => null),
                refreshToken: vi.fn(async () => false),
                logout: vi.fn(),
            };
            registerAuthCallbacks(callbacks);

            const rejected = getResponseRejectedInterceptor();
            const protectedEndpointError = {
                config: {url: '/subscriptions', headers: {}},
                response: {status: 401},
            };

            await expect(rejected(protectedEndpointError)).rejects.toBe(protectedEndpointError);
            expect(callbacks.refreshToken).toHaveBeenCalledTimes(1);
            expect(callbacks.logout).toHaveBeenCalledTimes(1);
        });
    });

    describe('Callback Behavior', () => {
        it('should allow callbacks to be invoked', async () => {
            const callbacks: AuthCallbacks = {
                getAccessToken: vi.fn(() => 'test-token'),
                refreshToken: vi.fn(async () => true),
                logout: vi.fn(),
            };

            registerAuthCallbacks(callbacks);

            // Verify callbacks can be called
            expect(callbacks.getAccessToken()).toBe('test-token');
            expect(await callbacks.refreshToken()).toBe(true);
            expect(() => callbacks.logout()).not.toThrow();
        });

        it('should support refresh token returning false on failure', async () => {
            const callbacks: AuthCallbacks = {
                getAccessToken: vi.fn(() => null),
                refreshToken: vi.fn(async () => false), // Refresh fails
                logout: vi.fn(),
            };

            registerAuthCallbacks(callbacks);

            const refreshResult = await callbacks.refreshToken();
            expect(refreshResult).toBe(false);
        });
    });
});
