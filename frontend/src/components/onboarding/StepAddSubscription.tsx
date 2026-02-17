'use client';

import { useState } from 'react';
import { Button, Input, Select } from '@/components/ui';
import { GradientText } from '@/components/ui/GradientText';
import ServiceSuggestionCard from './ServiceSuggestionCard';
import { popularServices } from '@/features/subscriptions/data/popular-services';
import type { PopularService, SubscriptionRequest, BillingCycle } from '@/features/subscriptions/types/subscription.types';

interface StepAddSubscriptionProps {
    onNext: (subscription: SubscriptionRequest) => void;
    onSubmitting?: (isSubmitting: boolean) => void;
}

export default function StepAddSubscription({ onNext, onSubmitting }: StepAddSubscriptionProps) {
    const [selectedService, setSelectedService] = useState<PopularService | null>(null);
    const [formData, setFormData] = useState<{
        name: string;
        value: string;
        billingCycle: BillingCycle;
        categoryId: string;
        nextPaymentDate: string;
    }>({
        name: '',
        value: '',
        billingCycle: 'MONTHLY',
        categoryId: '',
        nextPaymentDate: '',
    });

    // Suggested services (first 8)
    const suggestions = popularServices.slice(0, 8);

    const handleServiceSelect = (service: PopularService) => {
        if (selectedService?.name === service.name) {
            // Deselect
            setSelectedService(null);
            setFormData({
                name: '',
                value: '',
                billingCycle: 'MONTHLY',
                categoryId: '',
                nextPaymentDate: '',
            });
        } else {
            // Select service
            setSelectedService(service);
            setFormData({
                name: service.name,
                value: service.defaultValue.toString(),
                billingCycle: service.defaultBillingCycle,
                categoryId: service.categoryId.toString(),
                nextPaymentDate: '',
            });
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Validate required fields
        if (!formData.name || !formData.value || !formData.nextPaymentDate) {
            alert('Por favor, preencha todos os campos obrigatórios');
            return;
        }

        const subscription: SubscriptionRequest = {
            name: formData.name,
            value: parseFloat(formData.value),
            billingCycle: formData.billingCycle,
            startDate: new Date().toISOString().split('T')[0],
            nextPaymentDate: formData.nextPaymentDate,
            active: true,
            notifyUser: true,
            currency: 'BRL',
            categoryId: formData.categoryId ? parseInt(formData.categoryId) : undefined,
            logoUrl: selectedService?.logoUrl,
        };

        onNext(subscription);
    };

    return (
        <div className="space-y-8 max-w-4xl mx-auto">
            {/* Header */}
            <div className="text-center mb-8">
                <h2 className="text-3xl font-bold mb-2 text-gray-900 dark:text-white">
                    Adicione sua primeira assinatura
                </h2>
                <p className="text-gray-600 dark:text-gray-400">
                    Escolha um serviço popular ou preencha manualmente
                </p>
            </div>

            {/* Service suggestions grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {suggestions.map((service) => (
                    <ServiceSuggestionCard
                        key={service.name}
                        service={{
                            name: service.name,
                            price: service.defaultValue,
                            category: service.categoryId.toString(),
                            color: service.brandColor || '#6B7280',
                        }}
                        isSelected={selectedService?.name === service.name}
                        onClick={() => handleServiceSelect(service)}
                    />
                ))}
            </div>

            {/* Divider */}
            <div className="relative">
                <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-gray-300 dark:border-gray-700" />
                </div>
                <div className="relative flex justify-center text-sm">
                    <span className="px-4 bg-gray-50 dark:bg-gray-900 text-gray-500">
                        ou preencha manualmente
                    </span>
                </div>
            </div>

            {/* Manual form */}
            <form onSubmit={handleSubmit} className="space-y-6 bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm">
                {/* Name */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Nome *
                    </label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        placeholder="Ex: Netflix, Spotify..."
                        required
                    />
                </div>

                {/* Value and Billing Cycle */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Valor (R$) *
                        </label>
                        <Input
                            type="number"
                            step="0.01"
                            value={formData.value}
                            onChange={(e) => setFormData({ ...formData, value: e.target.value })}
                            placeholder="0,00"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Ciclo de cobrança *
                        </label>
                        <Select
                            value={formData.billingCycle}
                            onChange={(e) => setFormData({ ...formData, billingCycle: e.target.value as BillingCycle })}
                            options={[
                                { value: 'WEEKLY', label: 'Semanal' },
                                { value: 'MONTHLY', label: 'Mensal' },
                                { value: 'QUARTERLY', label: 'Trimestral' },
                                { value: 'YEARLY', label: 'Anual' },
                            ]}
                        />
                    </div>
                </div>

                {/* Category (optional for now) */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Categoria (opcional)
                    </label>
                    <Select
                        value={formData.categoryId}
                        onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                        placeholder="Selecione..."
                        options={[
                            { value: '1', label: 'Video Streaming' },
                            { value: '2', label: 'Music Streaming' },
                            { value: '3', label: 'Gaming' },
                            { value: '4', label: 'Software/SaaS' },
                            { value: '5', label: 'Educação' },
                            { value: '6', label: 'Saúde' },
                            { value: '7', label: 'Utilidades' },
                            { value: '8', label: 'Seguros' },
                            { value: '9', label: 'Outros' },
                        ]}
                    />
                </div>

                {/* Next Payment Date */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Próximo vencimento *
                    </label>
                    <Input
                        type="date"
                        value={formData.nextPaymentDate}
                        onChange={(e) => setFormData({ ...formData, nextPaymentDate: e.target.value })}
                        required
                    />
                </div>

                {/* Submit button */}
                <Button
                    type="submit"
                    size="lg"
                    className="w-full"
                >
                    Adicionar e continuar →
                </Button>
            </form>
        </div>
    );
}
