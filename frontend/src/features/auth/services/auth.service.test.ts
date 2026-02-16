/**
 * Auth Service Tests
 *
 * Unit tests for authentication API methods and JWT utilities.
 */

import {beforeEach, describe, expect, it, vi} from 'vitest';
import {authService} from './auth.service';
import {apiClient} from './api-client';
import {API_ENDPOINTS} from '@/lib/constants';
import type {AuthResponse} from '../types/auth.types';

// Mock apiClient
vi.mock('./api-client', () => ({
    apiClient: {
        post: vi.fn(),
    },
}));

describe('authService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('register', () => {
        it('should call POST /auth/register with correct payload', async () => {
            const mockResponse: AuthResponse = {
                accessToken: 'mock-access-token',
                refreshToken: 'mock-refresh-token',
            };

            vi.mocked(apiClient.post).mockResolvedValue({data: mockResponse});

            const result = await authService.register('John Doe', 'john@example.com', 'password123');

            expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.REGISTER, {
                name: 'John Doe',
                email: 'john@example.com',
                password: 'password123',
            });
            expect(result).toEqual(mockResponse);
        });

        it('should propagate errors from API', async () => {
            const mockError = new Error('Email already exists');
            vi.mocked(apiClient.post).mockRejectedValue(mockError);

            await expect(
                authService.register('John Doe', 'john@example.com', 'password123')
            ).rejects.toThrow('Email already exists');
        });
    });

    describe('login', () => {
        it('should call POST /auth/login with correct payload', async () => {
            const mockResponse: AuthResponse = {
                accessToken: 'mock-access-token',
                refreshToken: 'mock-refresh-token',
            };

            vi.mocked(apiClient.post).mockResolvedValue({data: mockResponse});

            const result = await authService.login('john@example.com', 'password123');

            expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.LOGIN, {
                email: 'john@example.com',
                password: 'password123',
            });
            expect(result).toEqual(mockResponse);
        });

        it('should propagate errors from API', async () => {
            const mockError = new Error('Invalid credentials');
            vi.mocked(apiClient.post).mockRejectedValue(mockError);

            await expect(authService.login('john@example.com', 'wrongpass')).rejects.toThrow(
                'Invalid credentials'
            );
        });
    });

    describe('refresh', () => {
        it('should call POST /auth/refresh with refresh token', async () => {
            const mockResponse: AuthResponse = {
                accessToken: 'new-access-token',
                refreshToken: 'new-refresh-token',
            };

            vi.mocked(apiClient.post).mockResolvedValue({data: mockResponse});

            const result = await authService.refresh('old-refresh-token');

            expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.REFRESH, {
                refreshToken: 'old-refresh-token',
            });
            expect(result).toEqual(mockResponse);
        });

        it('should propagate errors from API', async () => {
            const mockError = new Error('Invalid refresh token');
            vi.mocked(apiClient.post).mockRejectedValue(mockError);

            await expect(authService.refresh('invalid-token')).rejects.toThrow('Invalid refresh token');
        });
    });

    describe('logout', () => {
        it('should be a no-op (client-side only)', () => {
            // Logout doesn't call the backend
            expect(() => authService.logout()).not.toThrow();
            expect(apiClient.post).not.toHaveBeenCalled();
        });
    });

    describe('decodeToken', () => {
        it('should decode valid JWT token', () => {
            // Create a mock JWT: header.payload.signature
            const payload = {
                sub: 'john@example.com',
                userId: 1,
                name: 'John Doe',
                role: 'USER',
                iat: 1234567890,
                exp: 1234567890 + 3600,
            };

            const encodedPayload = btoa(JSON.stringify(payload));
            const mockToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${encodedPayload}.signature`;

            const decoded = authService.decodeToken(mockToken);

            expect(decoded).toEqual(payload);
        });

        it('should return null for invalid JWT format', () => {
            const invalidToken = 'not.a.valid.jwt.token';
            const decoded = authService.decodeToken(invalidToken);

            expect(decoded).toBeNull();
        });

        it('should return null for malformed base64', () => {
            const invalidToken = 'header.invalid-base64.signature';
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {
            });

            const decoded = authService.decodeToken(invalidToken);

            expect(decoded).toBeNull();
            expect(consoleErrorSpy).toHaveBeenCalled();

            consoleErrorSpy.mockRestore();
        });

        it('should return null for invalid JSON in payload', () => {
            const invalidPayload = btoa('{ invalid json }');
            const invalidToken = `header.${invalidPayload}.signature`;
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {
            });

            const decoded = authService.decodeToken(invalidToken);

            expect(decoded).toBeNull();
            expect(consoleErrorSpy).toHaveBeenCalled();

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getUserFromToken', () => {
        it('should extract User object from valid access token', () => {
            const payload = {
                sub: 'john@example.com',
                userId: 1,
                name: 'John Doe',
                role: 'USER',
                iat: 1234567890,
                exp: 1234567890 + 3600,
            };

            const encodedPayload = btoa(JSON.stringify(payload));
            const mockToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${encodedPayload}.signature`;

            const user = authService.getUserFromToken(mockToken);

            expect(user).toEqual({
                id: 1,
                name: 'John Doe',
                email: 'john@example.com',
                role: 'USER',
            });
        });

        it('should return null for invalid token', () => {
            const invalidToken = 'invalid.token.here';
            const user = authService.getUserFromToken(invalidToken);

            expect(user).toBeNull();
        });

        it('should return null when decoding fails', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {
            });
            const user = authService.getUserFromToken('malformed-token');

            expect(user).toBeNull();

            consoleErrorSpy.mockRestore();
        });
    });
});
