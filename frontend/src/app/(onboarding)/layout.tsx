import type { Metadata } from 'next';

export const metadata: Metadata = {
    title: 'Onboarding | Ongoing',
    description: 'Setup your Ongoing account',
};

export default function OnboardingLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
            {children}
        </div>
    );
}
