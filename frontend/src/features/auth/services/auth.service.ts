/**
 * Auth Service
 *
 * API methods for authentication operations and JWT utilities.
 *
 * Methods:
 * - register: Create new user account
 * - login: Authenticate user
 * - refresh: Refresh access token
 * - logout: Client-side logout (no backend call needed)
 * - decodeToken: Decode JWT payload
 * - getUserFromToken: Extract User object from JWT
 */

import {apiClient} from './api-client';
import {API_ENDPOINTS} from '@/lib/constants';
import type {AuthResponse, JwtPayload, LoginRequest, RefreshRequest, RegisterRequest, User,} from '../types/auth.types';

/**
 * Register a new user
 * @param name - User's full name
 * @param email - User's email
 * @param password - User's password
 * @returns AuthResponse with access and refresh tokens
 */
export const register = async (
    name: string,
    email: string,
    password: string
): Promise<AuthResponse> => {
    const payload: RegisterRequest = {name, email, password};

    const response = await apiClient.post<AuthResponse>(
        API_ENDPOINTS.AUTH.REGISTER,
        payload
    );

    return response.data;
};

/**
 * Login user
 * @param email - User's email
 * @param password - User's password
 * @returns AuthResponse with access and refresh tokens
 */
export const login = async (
    email: string,
    password: string
): Promise<AuthResponse> => {
    const payload: LoginRequest = {email, password};

    const response = await apiClient.post<AuthResponse>(
        API_ENDPOINTS.AUTH.LOGIN,
        payload
    );

    return response.data;
};

/**
 * Refresh access token using refresh token
 * @param refreshToken - Current refresh token
 * @returns AuthResponse with new access and refresh tokens
 */
export const refresh = async (refreshToken: string): Promise<AuthResponse> => {
    const payload: RefreshRequest = {refreshToken};

    const response = await apiClient.post<AuthResponse>(
        API_ENDPOINTS.AUTH.REFRESH,
        payload
    );

    return response.data;
};

/**
 * Logout user (client-side only)
 *
 * Note: Backend does NOT have a logout endpoint.
 * Tokens are one-time use (refresh token rotation) and expire naturally.
 * Client-side logout just clears local state and localStorage.
 */
export const logout = (): void => {
    // No backend call needed
    // Tokens will expire naturally
    // Client clears state in AuthContext
};

/**
 * Decode JWT token payload
 * @param token - JWT access token
 * @returns Decoded JWT payload
 *
 * NOTE: This does NOT validate the signature.
 * Signature validation happens on the backend.
 * This is only for extracting user data from the token.
 */
export const decodeToken = (token: string): JwtPayload | null => {
    try {
        // JWT format: header.payload.signature
        const parts = token.split('.');

        if (parts.length !== 3) {
            console.error('Invalid JWT format');
            return null;
        }

        // Decode base64 payload (middle part)
        const payload = parts[1];
        const decodedPayload = atob(payload);
        const parsedPayload: JwtPayload = JSON.parse(decodedPayload);

        return parsedPayload;
    } catch (error) {
        console.error('Error decoding JWT token:', error);
        return null;
    }
};

/**
 * Extract User object from JWT access token
 * @param accessToken - JWT access token
 * @returns User object or null if invalid
 */
export const getUserFromToken = (accessToken: string): User | null => {
    const payload = decodeToken(accessToken);

    if (!payload) {
        return null;
    }

    return {
        id: payload.userId,
        name: payload.name,
        email: payload.sub, // Subject is the email
        role: payload.role,
    };
};

/**
 * Export all auth service methods
 */
export const authService = {
    register,
    login,
    refresh,
    logout,
    decodeToken,
    getUserFromToken,
};
