'use client';

interface ProgressBarProps {
    currentStep: number;
    totalSteps: number;
    steps: string[];
}

export default function ProgressBar({ currentStep, totalSteps, steps }: ProgressBarProps) {
    const progress = (currentStep / totalSteps) * 100;

    return (
        <div className="w-full max-w-2xl mx-auto mb-12">
            {/* Progress bar */}
            <div className="relative h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden mb-4">
                <div
                    className="absolute top-0 left-0 h-full bg-gradient-to-r from-primary to-accent transition-all duration-500 ease-out"
                    style={{ width: `${progress}%` }}
                />
            </div>

            {/* Step labels */}
            <div className="flex justify-between items-center">
                {steps.map((step, index) => {
                    const stepNumber = index + 1;
                    const isActive = stepNumber === currentStep;
                    const isCompleted = stepNumber < currentStep;

                    return (
                        <div
                            key={step}
                            className="flex flex-col items-center"
                        >
                            <div
                                className={`
                                    w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
                                    transition-all duration-300
                                    ${isActive
                                        ? 'bg-primary text-white scale-110 shadow-glow'
                                        : isCompleted
                                            ? 'bg-primary text-white'
                                            : 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500'
                                    }
                                `}
                            >
                                {isCompleted ? '✓' : stepNumber}
                            </div>
                            <span
                                className={`
                                    text-xs mt-2 font-medium transition-colors duration-300
                                    ${isActive
                                        ? 'text-primary'
                                        : isCompleted
                                            ? 'text-gray-700 dark:text-gray-300'
                                            : 'text-gray-400 dark:text-gray-500'
                                    }
                                `}
                            >
                                {step}
                            </span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
