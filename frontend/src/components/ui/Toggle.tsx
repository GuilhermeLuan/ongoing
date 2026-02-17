import {cn} from "@/lib/utils";

interface ToggleProps {
    checked: boolean;
    onChange: (checked: boolean) => void;
    label?: string;
    disabled?: boolean;
}

export function Toggle({checked, onChange, label, disabled = false}: ToggleProps) {
    return (
        <div
            className={cn(
                "inline-flex items-center gap-3",
                disabled && "opacity-50"
            )}
        >
            {label && (
                <span className="text-sm font-medium text-neutral-700">{label}</span>
            )}
            <button
                type="button"
                role="switch"
                aria-checked={checked}
                aria-label={label}
                disabled={disabled}
                onClick={() => !disabled && onChange(!checked)}
                className={cn(
                    "relative inline-flex h-[26px] w-[46px] shrink-0 rounded-full cursor-pointer",
                    "transition-colors duration-200 ease-in-out",
                    "focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500/40 focus-visible:ring-offset-2",
                    "disabled:cursor-not-allowed",
                    checked ? "bg-primary-500" : "bg-neutral-300"
                )}
            >
                <span
                    className={cn(
                        "pointer-events-none inline-block h-[22px] w-[22px] rounded-full bg-white shadow-md",
                        "transform transition-transform duration-200 ease-in-out",
                        "translate-y-[2px]",
                        checked ? "translate-x-[22px]" : "translate-x-[2px]"
                    )}
                />
            </button>
        </div>
    );
}
