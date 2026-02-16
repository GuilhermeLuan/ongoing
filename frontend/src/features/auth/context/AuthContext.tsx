'use client';

/**
 * Auth Context
 *
 * Global authentication state management with React Context.
 *
 * Features:
 * - User authentication state (user, accessToken)
 * - Auth operations (login, register, logout, refresh)
 * - Silent refresh on app load
 * - Error handling
 * - Loading states
 * - Callback registration for api-client interceptors
 */

import React, {
    createContext,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import {authService} from '../services/auth.service';
import {tokenStorage} from '../utils/token-storage';
import {registerAuthCallbacks} from '../services/api-client';
import type {AuthContextValue, AuthState, User} from '../types/auth.types';

/**
 * Initial auth state
 */
const initialState: AuthState = {
    user: null,
    accessToken: null,
    isLoading: false,
    error: null,
    isInitialized: false,
};

/**
 * Auth Context
 */
export const AuthContext = createContext<AuthContextValue | undefined>(
    undefined
);

/**
 * Auth Provider Component
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
                                                                           children,
                                                                       }) => {
    const [state, setState] = useState<AuthState>(initialState);
    const refreshAttempted = useRef(false);

    /**
     * Clear error message
     */
    const clearError = useCallback(() => {
        setState((prev) => ({...prev, error: null}));
    }, []);

    /**
     * Set loading state
     */
    const setLoading = useCallback((isLoading: boolean) => {
        setState((prev) => ({...prev, isLoading}));
    }, []);

    /**
     * Set error message
     */
    const setError = useCallback((error: string) => {
        setState((prev) => ({...prev, error, isLoading: false}));
    }, []);

    /**
     * Set authenticated user with tokens
     */
    const setAuth = useCallback((user: User, accessToken: string) => {
        setState((prev) => ({
            ...prev,
            user,
            accessToken,
            isLoading: false,
            error: null,
        }));
    }, []);

    /**
     * Clear auth state (logout)
     */
    const clearAuth = useCallback(() => {
        setState((prev) => ({
            ...prev,
            user: null,
            accessToken: null,
            isLoading: false,
            error: null,
        }));
    }, []);

    /**
     * Register new user
     */
    const register = useCallback(
        async (name: string, email: string, password: string): Promise<void> => {
            try {
                setLoading(true);
                clearError();

                // Call backend API
                const response = await authService.register(name, email, password);

                // Extract user from access token
                const user = authService.getUserFromToken(response.accessToken);

                if (!user) {
                    throw new Error('Invalid access token received');
                }

                // Save refresh token to localStorage
                tokenStorage.setRefreshToken(response.refreshToken);

                // Update state with user and access token (in memory)
                setAuth(user, response.accessToken);
            } catch (error: unknown) {
                const errorMessage =
                    (error as {
                        response?: { data?: { message?: string } };
                        message?: string
                    }).response?.data?.message ||
                    (error as { message?: string }).message ||
                    'Failed to register. Please try again.';
                setError(errorMessage);
                throw error;
            }
        },
        [setLoading, clearError, setAuth, setError]
    );

    /**
     * Login user
     */
    const login = useCallback(
        async (email: string, password: string): Promise<void> => {
            try {
                setLoading(true);
                clearError();

                // Call backend API
                const response = await authService.login(email, password);

                // Extract user from access token
                const user = authService.getUserFromToken(response.accessToken);

                if (!user) {
                    throw new Error('Invalid access token received');
                }

                // Save refresh token to localStorage
                tokenStorage.setRefreshToken(response.refreshToken);

                // Update state with user and access token (in memory)
                setAuth(user, response.accessToken);
            } catch (error: unknown) {
                const errorMessage =
                    (error as {
                        response?: { data?: { message?: string } };
                        message?: string
                    }).response?.data?.message ||
                    (error as { message?: string }).message ||
                    'Failed to login. Please check your credentials.';
                setError(errorMessage);
                throw error;
            }
        },
        [setLoading, clearError, setAuth, setError]
    );

    /**
     * Logout user
     */
    const logout = useCallback(() => {
        // Clear refresh token from localStorage
        tokenStorage.removeRefreshToken();

        // Clear auth state
        clearAuth();

        // No backend call needed (tokens expire naturally)
        authService.logout();
    }, [clearAuth]);

    /**
     * Refresh access token
     * @returns True if refresh succeeded, false otherwise
     */
    const refreshAuth = useCallback(async (): Promise<boolean> => {
        try {
            // Get refresh token from localStorage
            const refreshToken = tokenStorage.getRefreshToken();

            if (!refreshToken) {
                // No refresh token available
                return false;
            }

            // Call backend API to refresh
            const response = await authService.refresh(refreshToken);

            // Extract user from new access token
            const user = authService.getUserFromToken(response.accessToken);

            if (!user) {
                console.error('Invalid access token received during refresh');
                return false;
            }

            // Save new refresh token to localStorage (token rotation)
            tokenStorage.setRefreshToken(response.refreshToken);

            // Update state with new user and access token
            setAuth(user, response.accessToken);

            return true;
        } catch (error: unknown) {
            console.error('Token refresh failed:', error);

            // Refresh failed, clear tokens and logout
            tokenStorage.removeRefreshToken();
            clearAuth();

            return false;
        }
    }, [setAuth, clearAuth]);

    /**
     * Silent refresh on app mount
     * Attempts to restore session from refresh token in localStorage
     */
    useEffect(() => {
        if (refreshAttempted.current) {
            return;
        }
        refreshAttempted.current = true;

        const silentRefresh = async () => {
            const hasRefreshToken = tokenStorage.hasRefreshToken();

            if (hasRefreshToken) {
                console.log('[Auth] Attempting silent refresh on mount...');
                const success = await refreshAuth();

                if (success) {
                    console.log('[Auth] Silent refresh succeeded');
                } else {
                    console.log('[Auth] Silent refresh failed');
                }
            }

            // Mark initialization as complete
            setState((prev) => ({...prev, isInitialized: true}));
        };

        silentRefresh();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Run only once on mount

    /**
     * Register auth callbacks for api-client interceptors
     * Must run whenever accessToken changes
     */
    useEffect(() => {
        registerAuthCallbacks({
            getAccessToken: () => state.accessToken,
            refreshToken: refreshAuth,
            logout: logout,
        });
    }, [state.accessToken, refreshAuth, logout]);

    /**
     * Context value (memoized to prevent unnecessary re-renders)
     */
    const value: AuthContextValue = useMemo(
        () => ({
            // State
            user: state.user,
            accessToken: state.accessToken,
            isLoading: state.isLoading,
            error: state.error,
            isInitialized: state.isInitialized,

            // Methods
            login,
            register,
            logout,
            refreshAuth,
            clearError,
        }),
        [
            state.user,
            state.accessToken,
            state.isLoading,
            state.error,
            state.isInitialized,
            login,
            register,
            logout,
            refreshAuth,
            clearError,
        ]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
