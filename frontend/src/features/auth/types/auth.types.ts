/**
 * Authentication Type Definitions
 *
 * All TypeScript interfaces and types for the authentication system.
 */

/**
 * User Model
 * Represents the authenticated user extracted from JWT payload
 */
export interface User {
    id: number;
    name: string;
    email: string;
    role: string;
}

/**
 * JWT Payload Structure
 * Decoded JWT token structure (matches backend implementation)
 */
export interface JwtPayload {
    sub: string;           // Subject (user email)
    userId: number;        // User ID
    name: string;          // User name
    role: string;          // User role
    iat: number;           // Issued at (timestamp)
    exp: number;           // Expiration (timestamp)
}

/**
 * Auth Response from Backend
 * Returned by login, register, and refresh endpoints
 */
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
}

/**
 * Login Request Payload
 */
export interface LoginRequest {
    email: string;
    password: string;
}

/**
 * Register Request Payload
 */
export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
}

/**
 * Refresh Token Request Payload
 */
export interface RefreshRequest {
    refreshToken: string;
}

/**
 * Auth State
 * Internal state managed by AuthContext
 */
export interface AuthState {
    user: User | null;              // Current authenticated user (null if logged out)
    accessToken: string | null;     // JWT access token (stored in memory, lost on refresh)
    isLoading: boolean;             // Loading state for auth operations
    error: string | null;           // Last error message
    isInitialized: boolean;         // Has silent refresh completed on mount?
}

/**
 * Auth Context Value
 * API exposed by AuthContext to consumers via useAuth()
 */
export interface AuthContextValue {
    // State
    user: User | null;
    accessToken: string | null;
    isLoading: boolean;
    error: string | null;
    isInitialized: boolean;

    // Methods
    login: (email: string, password: string) => Promise<void>;
    register: (name: string, email: string, password: string) => Promise<void>;
    logout: () => void;
    refreshAuth: () => Promise<boolean>;
    clearError: () => void;
}

/**
 * Auth Callbacks
 * Callbacks registered by AuthContext for use by api-client interceptors
 * This pattern avoids circular dependency between api-client and AuthContext
 */
export interface AuthCallbacks {
    getAccessToken: () => string | null;
    refreshToken: () => Promise<boolean>;
    logout: () => void;
}
