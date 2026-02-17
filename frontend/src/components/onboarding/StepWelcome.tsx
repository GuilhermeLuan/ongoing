'use client';

import { Button } from '@/components/ui';
import GradientText from '@/components/ui/GradientText';

interface StepWelcomeProps {
    userName: string;
    onNext: () => void;
}

export default function StepWelcome({ userName, onNext }: StepWelcomeProps) {
    return (
        <div className="text-center space-y-8 animate-fadeInUp">
            {/* Emoji */}
            <div className="text-6xl mb-6 animate-bounce">👋</div>

            {/* Headline */}
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
                Bem-vindo ao <GradientText>Ongoing</GradientText>, {userName}!
            </h1>

            {/* Description */}
            <p className="text-lg text-gray-600 dark:text-gray-300 mb-8">
                Vamos configurar tudo pra você em poucos passos.<br />
                Em menos de 2 minutos você terá:
            </p>

            {/* Benefits list */}
            <div className="space-y-4 max-w-md mx-auto text-left">
                {[
                    'Sua primeira assinatura cadastrada',
                    'Visão completa dos seus gastos',
                    'Controle dos próximos vencimentos',
                ].map((benefit, index) => (
                    <div
                        key={benefit}
                        className="flex items-start gap-3 animate-fadeInUp"
                        style={{ animationDelay: `${index * 100}ms` }}
                    >
                        <svg
                            className="w-6 h-6 text-primary mt-1 flex-shrink-0"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M5 13l4 4L19 7"
                            />
                        </svg>
                        <span className="text-gray-700 dark:text-gray-300">{benefit}</span>
                    </div>
                ))}
            </div>

            {/* Decorative blobs - optional, keep simple for now */}
            <div className="relative h-32 mt-12">
                <div className="absolute inset-0 flex items-center justify-center gap-4 opacity-20">
                    <div className="w-16 h-16 bg-primary rounded-full animate-pulse" />
                    <div className="w-20 h-20 bg-accent rounded-full animate-pulse" style={{ animationDelay: '300ms' }} />
                    <div className="w-12 h-12 bg-primary rounded-full animate-pulse" style={{ animationDelay: '600ms' }} />
                </div>
            </div>

            {/* CTA Button */}
            <Button
                onClick={onNext}
                size="lg"
                className="mt-8"
            >
                Vamos começar →
            </Button>
        </div>
    );
}
