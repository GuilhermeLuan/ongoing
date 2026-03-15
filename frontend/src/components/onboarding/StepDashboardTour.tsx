'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui';

interface StepDashboardTourProps {
    onNext: () => void;
}

export default function StepDashboardTour({ onNext }: StepDashboardTourProps) {
    const [currentSlide, setCurrentSlide] = useState(0);
    const [hasViewedAll, setHasViewedAll] = useState(false);

    const slides = [
        {
            title: 'Seus gastos e assinaturas em tempo real',
            highlight: 'stats',
        },
        {
            title: 'Nunca perca um vencimento — veja o que vem aí',
            highlight: 'upcoming',
        },
        {
            title: 'Entenda pra onde seu dinheiro vai, por categoria',
            highlight: 'chart',
        },
    ];

    // Auto-advance slides
    useEffect(() => {
        const timer = setTimeout(() => {
            if (currentSlide < slides.length - 1) {
                setCurrentSlide(currentSlide + 1);
            } else if (!hasViewedAll) {
                setHasViewedAll(true);
            }
        }, 4000);

        return () => clearTimeout(timer);
    }, [currentSlide, hasViewedAll]);

    const handleDotClick = (index: number) => {
        setCurrentSlide(index);
        if (index === slides.length - 1) {
            setHasViewedAll(true);
        }
    };

    return (
        <div className="space-y-8 max-w-4xl mx-auto">
            {/* Header */}
            <div className="text-center mb-8">
                <h2 className="text-3xl font-bold mb-2 text-gray-900 dark:text-white">
                    Conheça seu painel de controle
                </h2>
                <p className="text-gray-600 dark:text-gray-400">
                    Tudo que você precisa, em um só lugar
                </p>
            </div>

            {/* Dashboard mockup */}
            <div className="relative bg-white dark:bg-gray-800 rounded-xl shadow-xl p-8 min-h-[400px]">
                {/* Stats cards */}
                <div
                    className={`grid grid-cols-4 gap-4 mb-6 transition-all duration-500 ${currentSlide === 0 ? 'ring-4 ring-primary ring-opacity-50 rounded-lg p-2' : ''
                        }`}
                >
                    {['R$ 286', '12', '3', '2d'].map((stat, i) => (
                        <div key={i} className="bg-gray-100 dark:bg-gray-700 rounded-lg p-4 text-center">
                            <div className="text-2xl font-bold text-gray-900 dark:text-white">{stat}</div>
                            <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                                {i === 0 ? 'Total' : i === 1 ? 'Assin.' : i === 2 ? 'Vencendo' : 'Próximo'}
                            </div>
                        </div>
                    ))}
                </div>

                {/* Content grid */}
                <div className="grid grid-cols-2 gap-4">
                    {/* Upcoming payments */}
                    <div
                        className={`bg-gray-100 dark:bg-gray-700 rounded-lg p-4 transition-all duration-500 ${currentSlide === 1 ? 'ring-4 ring-primary ring-opacity-50' : ''
                            }`}
                    >
                        <h3 className="font-semibold mb-3 text-gray-900 dark:text-white">Próximos vencimentos</h3>
                        <div className="space-y-2">
                            {[1, 2, 3].map((i) => (
                                <div key={i} className="flex items-center gap-2">
                                    <div className="w-8 h-8 bg-gray-300 dark:bg-gray-600 rounded" />
                                    <div className="h-4 bg-gray-300 dark:bg-gray-600 rounded flex-1" />
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Chart */}
                    <div
                        className={`bg-gray-100 dark:bg-gray-700 rounded-lg p-4 transition-all duration-500 ${currentSlide === 2 ? 'ring-4 ring-primary ring-opacity-50' : ''
                            }`}
                    >
                        <h3 className="font-semibold mb-3 text-gray-900 dark:text-white">Por categoria</h3>
                        <div className="space-y-2">
                            {[
                                { width: '75%', label: '40%' },
                                { width: '50%', label: '25%' },
                                { width: '35%', label: '15%' },
                            ].map((bar, i) => (
                                <div key={i}>
                                    <div className="flex items-center gap-2 mb-1">
                                        <div className="text-xs text-gray-600 dark:text-gray-400">{bar.label}</div>
                                    </div>
                                    <div className="h-6 bg-primary rounded" style={{ width: bar.width }} />
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Highlight indicator (animated circle) */}
                {currentSlide !== null && (
                    <div
                        className="absolute top-4 right-4 text-4xl animate-pulse"
                        key={currentSlide}
                    >
                        ✨
                    </div>
                )}
            </div>

            {/* Description text */}
            <div className="text-center min-h-[60px]">
                <p
                    className="text-lg text-gray-700 dark:text-gray-300 animate-fadeInUp"
                    key={currentSlide}
                >
                    {slides[currentSlide].title}
                </p>
            </div>

            {/* Navigation dots */}
            <div className="flex justify-center gap-2 mb-8">
                {slides.map((_, index) => (
                    <button
                        key={index}
                        onClick={() => handleDotClick(index)}
                        className={`w-3 h-3 rounded-full transition-all duration-300 ${currentSlide === index
                                ? 'bg-primary w-8'
                                : 'bg-gray-300 dark:bg-gray-600'
                            }`}
                        aria-label={`Go to slide ${index + 1}`}
                    />
                ))}
            </div>

            {/* Next button */}
            <div className="flex justify-end">
                <Button
                    onClick={onNext}
                    size="lg"
                    disabled={!hasViewedAll}
                    className={`transition-opacity ${hasViewedAll ? 'opacity-100' : 'opacity-50 cursor-not-allowed'}`}
                >
                    Próximo →
                </Button>
            </div>
        </div>
    );
}
