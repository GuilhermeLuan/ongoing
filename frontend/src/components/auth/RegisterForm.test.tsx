import {beforeEach, describe, expect, it, vi} from 'vitest';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {RegisterForm} from './RegisterForm';

const mockRegister = vi.fn();
const mockClearError = vi.fn();
const mockPush = vi.fn();

vi.mock('@/features/auth', () => ({
    useAuth: () => ({
        register: mockRegister,
        error: null,
        clearError: mockClearError,
    }),
}));

vi.mock('next/navigation', () => ({
    useRouter: () => ({
        push: mockPush,
    }),
    useSearchParams: () => ({
        get: () => null,
    }),
}));

describe('RegisterForm', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockRegister.mockResolvedValue(undefined);
    });

    it('should require at least 8 characters for password', async () => {
        const user = userEvent.setup();
        render(<RegisterForm/>);

        await user.type(screen.getByLabelText('Nome completo'), 'Test User');
        await user.type(screen.getByLabelText('Email'), 'test@example.com');
        await user.type(screen.getByLabelText('Senha'), '1234567');
        await user.type(screen.getByLabelText('Confirmar senha'), '1234567');
        await user.click(screen.getByRole('button', {name: 'Criar conta'}));

        expect(await screen.findByText('Senha deve ter no mínimo 8 caracteres')).toBeInTheDocument();
        expect(mockRegister).not.toHaveBeenCalled();
    });

    it('should submit when password has 8 or more characters', async () => {
        const user = userEvent.setup();
        render(<RegisterForm/>);

        await user.type(screen.getByLabelText('Nome completo'), 'Test User');
        await user.type(screen.getByLabelText('Email'), 'test@example.com');
        await user.type(screen.getByLabelText('Senha'), '12345678');
        await user.type(screen.getByLabelText('Confirmar senha'), '12345678');
        await user.click(screen.getByRole('button', {name: 'Criar conta'}));

        await waitFor(() => {
            expect(mockRegister).toHaveBeenCalledWith('Test User', 'test@example.com', '12345678');
            expect(mockPush).toHaveBeenCalledWith('/dashboard');
        });
    });
});
