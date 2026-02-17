/**
 * Auth Module - Barrel Export
 *
 * Central export point for the authentication feature.
 * Enables clean imports from consumers.
 *
 * Usage:
 * ```typescript
 * import { useAuth, AuthProvider } from '@/features/auth';
 * ```
 */

// Context and Provider
export {AuthContext, AuthProvider} from './context/AuthContext';

// Hooks
export {useAuth} from './hooks/useAuth';

// Services
export {authService} from './services/auth.service';
export {apiClient, registerAuthCallbacks} from './services/api-client';

// Utilities
export {tokenStorage} from './utils/token-storage';
export {getFirstName} from './utils/user-utils';

// Types
export type {
    User,
    AuthResponse,
    LoginRequest,
    RegisterRequest,
    RefreshRequest,
    AuthState,
    AuthContextValue,
    AuthCallbacks,
    JwtPayload,
} from './types/auth.types';
