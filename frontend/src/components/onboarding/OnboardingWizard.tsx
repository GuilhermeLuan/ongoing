'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/features/auth';
import { subscriptionService } from '@/features/subscriptions';
import { userService } from '@/features/user/user.service';
import ProgressBar from './ProgressBar';
import StepWelcome from './StepWelcome';
import StepAddSubscription from './StepAddSubscription';
import StepDashboardTour from './StepDashboardTour';
import StepSuccess from './StepSuccess';
import type { SubscriptionRequest, SubscriptionResponse } from '@/features/subscriptions/types/subscription.types';

const STEPS = ['Bem-vindo', 'Primeira sub', 'Tour', 'Pronto!'];

export default function OnboardingWizard() {
    const router = useRouter();
    const { user } = useAuth();
    const [currentStep, setCurrentStep] = useState(1);
    const [createdSubscription, setCreatedSubscription] = useState<SubscriptionResponse | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Redirect if no user
    if (!user) {
        router.push('/login');
        return null;
    }

    // Redirect if onboarding already completed
    if (user.onboardingCompleted) {
        router.push('/dashboard');
        return null;
    }

    const handleNextStep = () => {
        setCurrentStep((prev) => Math.min(prev + 1, STEPS.length));
    };

    const handleAddSubscription = async (subscription: SubscriptionRequest) => {
        try {
            setIsSubmitting(true);
            const created = await subscriptionService.create(subscription);
            setCreatedSubscription(created);
            handleNextStep();
        } catch (error) {
            console.error('Error creating subscription:', error);
            alert('Erro ao criar assinatura. Por favor, tente novamente.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleFinish = async () => {
        try {
            setIsSubmitting(true);
            // Mark onboarding as completed
            await userService.updateCurrentUser({ onboardingCompleted: true });
            
            // Redirect to dashboard
            router.push('/dashboard');
        } catch (error) {
            console.error('Error completing onboarding:', error);
            alert('Erro ao finalizar onboarding. Por favor, tente novamente.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center p-6">
            {/* Logo */}
            <div className="mb-8">
                <h1 className="text-2xl font-bold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
                    Ongoing
                </h1>
            </div>

            {/* Progress Bar */}
            <ProgressBar
                currentStep={currentStep}
                totalSteps={STEPS.length}
                steps={STEPS}
            />

            {/* Step Content */}
            <div className="w-full max-w-4xl">
                {currentStep === 1 && (
                    <StepWelcome
                        userName={user.name}
                        onNext={handleNextStep}
                    />
                )}

                {currentStep === 2 && (
                    <StepAddSubscription
                        onNext={handleAddSubscription}
                        onSubmitting={setIsSubmitting}
                    />
                )}

                {currentStep === 3 && (
                    <StepDashboardTour
                        onNext={handleNextStep}
                    />
                )}

                {currentStep === 4 && (
                    <StepSuccess
                        userName={user.name}
                        subscription={createdSubscription}
                        onFinish={handleFinish}
                    />
                )}
            </div>

            {/* Loading overlay */}
            {isSubmitting && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white dark:bg-gray-800 rounded-lg p-6">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
                        <p className="mt-4 text-gray-700 dark:text-gray-300">Carregando...</p>
                    </div>
                </div>
            )}
        </div>
    );
}
