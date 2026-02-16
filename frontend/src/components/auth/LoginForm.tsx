"use client";

import {FormEvent, useState} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import Link from "next/link";
import {Lock, Mail} from "lucide-react";
import {Button, Input} from "@/components/ui";
import {AuthAlert} from "./AuthAlert";
import {useAuth} from "@/features/auth";

export function LoginForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const {login, error, clearError} = useAuth();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ email?: string; password?: string }>(
        {}
    );
    const [isSubmitting, setIsSubmitting] = useState(false);

    function validate(): boolean {
        const newErrors: typeof errors = {};

        if (!email.trim()) {
            newErrors.email = "Email é obrigatório";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            newErrors.email = "Email inválido";
        }

        if (!password) {
            newErrors.password = "Senha é obrigatória";
        } else if (password.length < 6) {
            newErrors.password = "Senha deve ter no mínimo 6 caracteres";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (!validate()) return;

        setIsSubmitting(true);
        clearError();

        try {
            await login(email, password);
            const redirect = searchParams.get("redirect");
            const destination = redirect && redirect.startsWith("/") ? redirect : "/dashboard";
            router.push(destination);
        } catch {
            // Error is already set in AuthContext
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="w-full max-w-[420px]">
            <div>
                <h1 className="text-2xl font-display font-bold text-neutral-900">
                    Entrar na sua conta
                </h1>
                <p className="text-neutral-500 mt-2">
                    Bem-vindo de volta! Entre com seus dados.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                <Input
                    label="Email"
                    type="email"
                    placeholder="seu@email.com"
                    icon={<Mail size={18}/>}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    error={errors.email}
                    autoComplete="email"
                />

                <div>
                    <Input
                        label="Senha"
                        type="password"
                        placeholder="••••••••"
                        icon={<Lock size={18}/>}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        error={errors.password}
                        autoComplete="current-password"
                    />
                    <div className="flex justify-end mt-1.5">
            <span className="text-sm text-primary-500 cursor-default">
              Esqueceu a senha?
            </span>
                    </div>
                </div>

                {error && <AuthAlert message={error}/>}

                <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    className="w-full"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Entrando..." : "Entrar"}
                </Button>
            </form>

            <p className="text-sm text-neutral-500 text-center mt-6">
                Não tem uma conta?{" "}
                <Link
                    href="/register"
                    className="text-primary-500 hover:text-primary-600 font-medium transition-colors"
                >
                    Registre-se
                </Link>
            </p>
        </div>
    );
}
