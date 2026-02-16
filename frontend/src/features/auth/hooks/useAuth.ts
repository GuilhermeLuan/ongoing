/**
 * useAuth Hook
 *
 * Custom React hook to consume AuthContext.
 * Provides access to authentication state and methods.
 *
 * Usage:
 * ```typescript
 * const { user, login, logout, isLoading } = useAuth();
 * ```
 *
 * Throws error if used outside of AuthProvider.
 */

import {useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import type {AuthContextValue} from '../types/auth.types';

/**
 * useAuth Hook
 * @returns AuthContextValue with auth state and methods
 * @throws Error if used outside AuthProvider
 */
export const useAuth = (): AuthContextValue => {
    const context = useContext(AuthContext);

    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
};
