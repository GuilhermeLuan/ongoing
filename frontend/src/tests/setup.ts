/**
 * Vitest Setup File
 *
 * Global test configuration and setup.
 * Runs before all test files.
 */

import {afterEach, expect} from 'vitest';
import {cleanup} from '@testing-library/react';
import * as matchers from '@testing-library/jest-dom/matchers';

// Extend Vitest's expect with jest-dom matchers
expect.extend(matchers);

// Cleanup after each test case (unmount React components)
afterEach(() => {
    cleanup();
});

// Mock localStorage for tests
const localStorageMock = (() => {
    let store: Record<string, string> = {};

    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => {
            store[key] = value.toString();
        },
        removeItem: (key: string) => {
            delete store[key];
        },
        clear: () => {
            store = {};
        },
    };
})();

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
});

// Mock window.matchMedia (needed for some components)
Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {
        }, // Deprecated
        removeListener: () => {
        }, // Deprecated
        addEventListener: () => {
        },
        removeEventListener: () => {
        },
        dispatchEvent: () => {
        },
    }),
});
