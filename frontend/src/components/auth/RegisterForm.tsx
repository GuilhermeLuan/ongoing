"use client";

import {FormEvent, useState} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import Link from "next/link";
import {Lock, Mail, User} from "lucide-react";
import {Button, Input} from "@/components/ui";
import {AuthAlert} from "./AuthAlert";
import {useAuth} from "@/features/auth";

export function RegisterForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const {register, error, clearError} = useAuth();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [errors, setErrors] = useState<{
        name?: string;
        email?: string;
        password?: string;
        confirmPassword?: string;
    }>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    function validate(): boolean {
        const newErrors: typeof errors = {};

        if (!name.trim()) {
            newErrors.name = "Nome é obrigatório";
        } else if (name.trim().length < 2) {
            newErrors.name = "Nome deve ter no mínimo 2 caracteres";
        }

        if (!email.trim()) {
            newErrors.email = "Email é obrigatório";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            newErrors.email = "Email inválido";
        }

        if (!password) {
            newErrors.password = "Senha é obrigatória";
        } else if (password.length < 8) {
            newErrors.password = "Senha deve ter no mínimo 8 caracteres";
        }

        if (!confirmPassword) {
            newErrors.confirmPassword = "Confirme sua senha";
        } else if (confirmPassword !== password) {
            newErrors.confirmPassword = "As senhas não coincidem";
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
            await register(name, email, password);
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
                    Criar sua conta
                </h1>
                <p className="text-neutral-500 mt-2">
                    Comece a gerenciar suas assinaturas hoje.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                <Input
                    label="Nome completo"
                    type="text"
                    placeholder="Seu nome"
                    icon={<User size={18}/>}
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    error={errors.name}
                    autoComplete="name"
                />

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

                <Input
                    label="Senha"
                    type="password"
                    placeholder="••••••••"
                    icon={<Lock size={18}/>}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    error={errors.password}
                    autoComplete="new-password"
                />

                <Input
                    label="Confirmar senha"
                    type="password"
                    placeholder="••••••••"
                    icon={<Lock size={18}/>}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    error={errors.confirmPassword}
                    autoComplete="new-password"
                />

                {error && <AuthAlert message={error}/>}

                <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    className="w-full"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Criando conta..." : "Criar conta"}
                </Button>
            </form>

            <p className="text-sm text-neutral-500 text-center mt-6">
                Já tem uma conta?{" "}
                <Link
                    href="/login"
                    className="text-primary-500 hover:text-primary-600 font-medium transition-colors"
                >
                    Entrar
                </Link>
            </p>
        </div>
    );
}
