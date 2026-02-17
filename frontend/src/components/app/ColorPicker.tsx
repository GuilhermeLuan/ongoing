"use client";

import {cn} from "@/lib/utils";
import {Check} from "lucide-react";

interface ColorPickerProps {
    value: string;
    onChange: (color: string) => void;
    label?: string;
}

const PRESET_COLORS = [
    "#E50914",
    "#1DB954",
    "#5865F2",
    "#0078D4",
    "#FF6600",
    "#10A37F",
    "#FF0080",
    "#FFC107",
    "#9C27B0",
    "#00BCD4",
    "#4CAF50",
    "#6B7280",
];

export function ColorPicker({value, onChange, label}: ColorPickerProps) {
    return (
        <div>
            {label && (
                <label className="block text-sm font-medium text-neutral-700 mb-2">
                    {label}
                </label>
            )}
            <div className="grid grid-cols-6 gap-2.5">
                {PRESET_COLORS.map((color) => {
                    const isSelected = value.toUpperCase() === color.toUpperCase();
                    return (
                        <button
                            key={color}
                            type="button"
                            onClick={() => onChange(color)}
                            className={cn(
                                "w-9 h-9 rounded-full cursor-pointer transition-all duration-200",
                                "hover:scale-110 active:scale-95",
                                "focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500",
                                "flex items-center justify-center",
                                isSelected
                                    ? "ring-2 ring-offset-2 ring-neutral-900 shadow-md"
                                    : "ring-1 ring-black/10 hover:ring-black/25"
                            )}
                            style={{backgroundColor: color}}
                            aria-label={`Selecionar cor ${color}`}
                            aria-pressed={isSelected}
                        >
                            {isSelected && (
                                <Check className="w-4 h-4 text-white drop-shadow-[0_1px_2px_rgba(0,0,0,0.5)]"/>
                            )}
                        </button>
                    );
                })}
            </div>
        </div>
    );
}
