'use client';

interface ServiceSuggestion {
    name: string;
    price: number;
    category: string;
    color: string;
}

interface ServiceSuggestionCardProps {
    service: ServiceSuggestion;
    isSelected: boolean;
    onClick: () => void;
}

export default function ServiceSuggestionCard({ service, isSelected, onClick }: ServiceSuggestionCardProps) {
    return (
        <button
            onClick={onClick}
            className={`
                group relative p-4 rounded-xl border-2 transition-all duration-200
                hover:scale-105 hover:shadow-lg
                ${isSelected
                    ? 'border-primary shadow-glow bg-white dark:bg-gray-800'
                    : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-primary/50'
                }
            `}
        >
            {/* Service icon/color */}
            <div
                className="w-12 h-12 rounded-lg mb-3 flex items-center justify-center text-2xl font-bold text-white"
                style={{ backgroundColor: service.color }}
            >
                {service.name.charAt(0)}
            </div>

            {/* Service name */}
            <h3 className="font-semibold text-sm text-gray-900 dark:text-white mb-1">
                {service.name}
            </h3>

            {/* Service price */}
            <p className="text-xs text-gray-600 dark:text-gray-400">
                R$ {service.price.toFixed(2).replace('.', ',')}
            </p>

            {/* Selected indicator */}
            {isSelected && (
                <div className="absolute top-2 right-2 w-5 h-5 bg-primary rounded-full flex items-center justify-center">
                    <svg
                        className="w-3 h-3 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={3}
                            d="M5 13l4 4L19 7"
                        />
                    </svg>
                </div>
            )}
        </button>
    );
}
