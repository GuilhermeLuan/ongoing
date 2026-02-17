'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui';
import GradientText from '@/components/ui/GradientText';
import ConfettiAnimation from './ConfettiAnimation';
import type { SubscriptionResponse } from '@/features/subscriptions/types/subscription.types';

interface StepSuccessProps {
    userName: string;
    subscription: SubscriptionResponse | null;
    onFinish: () => void;
}

export default function StepSuccess({ userName, subscription, onFinish }: StepSuccessProps) {
    const [showConfetti, setShowConfetti] = useState(true);

    useEffect(() => {
        // Hide confetti after a few seconds
        const timer = setTimeout(() => {
            setShowConfetti(false);
        }, 3000);

        return () => clearTimeout(timer);
    }, []);

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('pt-BR');
    };

    const formatPrice = (value: number) => {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL',
        }).format(value);
    };

    return (
        <div className="text-center space-y-8 animate-fadeInUp">
            {/* Confetti */}
            {showConfetti && <ConfettiAnimation />}

            {/* Emoji */}
            <div className="text-6xl mb-6 animate-scaleIn">🎉</div>

            {/* Headline */}
            <h1 className="text-4xl md:text-5xl font-bold mb-4 animate-scaleIn">
                Tudo pronto, <GradientText>{userName}</GradientText>!
            </h1>

            {/* Description */}
            <p className="text-lg text-gray-600 dark:text-gray-300 mb-8">
                Seu Ongoing está configurado e funcionando.
            </p>

            {/* Subscription summary */}
            {subscription && (
                <div className="max-w-md mx-auto bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 mb-8">
                    <div className="flex items-start gap-4">
                        <div className="flex-shrink-0">
                            <div className="w-12 h-12 bg-primary rounded-lg flex items-center justify-center text-white text-xl font-bold">
                                ✓
                            </div>
                        </div>
                        <div className="flex-1 text-left">
                            <h3 className="font-semibold text-lg text-gray-900 dark:text-white mb-1">
                                {subscription.name} adicionada
                            </h3>
                            <p className="text-sm text-gray-600 dark:text-gray-400">
                                {formatPrice(subscription.value)}/{subscription.billingCycle === 'MONTHLY' ? 'mês' : 'ano'} · 
                                Vence em {formatDate(subscription.nextPaymentDate)}
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Next steps */}
            <div className="max-w-md mx-auto space-y-4 text-left mb-8">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 text-center">
                    Próximos passos:
                </h3>
                {[
                    { icon: '📋', text: 'Adicione mais assinaturas pelo dashboard' },
                    { icon: '🔍', text: 'Use filtros pra encontrar qualquer uma' },
                    { icon: '📅', text: 'Fique de olho nos vencimentos da semana' },
                ].map((step, index) => (
                    <div
                        key={step.text}
                        className="flex items-start gap-3 animate-fadeInUp"
                        style={{ animationDelay: `${index * 100}ms` }}
                    >
                        <span className="text-2xl flex-shrink-0">{step.icon}</span>
                        <span className="text-gray-700 dark:text-gray-300 pt-1">
                            {step.text}
                        </span>
                    </div>
                ))}
            </div>

            {/* CTA Button */}
            <Button
                onClick={onFinish}
                size="lg"
                className="mt-8"
            >
                Ir para o Dashboard →
            </Button>
        </div>
    );
}
