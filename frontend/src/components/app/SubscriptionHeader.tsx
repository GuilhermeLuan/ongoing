"use client";

import {useState} from "react";
import {Pencil} from "lucide-react";
import {cn} from "@/lib/utils";

interface SubscriptionHeaderProps {
    name: string;
    logoUrl?: string;
    avatarColor: string;
    onEdit: () => void;
}

export function SubscriptionHeader({name, logoUrl, avatarColor, onEdit}: SubscriptionHeaderProps) {
    const [imageError, setImageError] = useState(false);

    const initials = name
        .split(" ")
        .map((word) => word[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);

    const showImage = logoUrl && !imageError;

    return (
        <div
            className="relative overflow-hidden rounded-xl p-5 transition-colors duration-300"
            style={{backgroundColor: avatarColor}}
        >
            {/* Subtle noise texture overlay */}
            <div
                className="absolute inset-0 opacity-[0.08] bg-[radial-gradient(circle_at_50%_50%,rgba(255,255,255,0.8),transparent_70%)]"/>

            <div className="relative flex items-center gap-4">
                {/* Logo Avatar - white background */}
                <div
                    className={cn(
                        "w-14 h-14 rounded-xl flex items-center justify-center shrink-0",
                        "bg-white shadow-md"
                    )}
                >
                    {showImage ? (
                        <img
                            src={logoUrl}
                            alt={name}
                            className="w-9 h-9 object-contain"
                            onError={() => setImageError(true)}
                        />
                    ) : (
                        <span
                            className="text-lg font-bold"
                            style={{color: avatarColor}}
                        >
                            {initials}
                        </span>
                    )}
                </div>

                {/* Service Name */}
                <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-semibold text-white truncate drop-shadow-[0_1px_2px_rgba(0,0,0,0.15)]">
                        {name}
                    </h3>
                    <p className="text-sm text-white/70">Personalizar aparência</p>
                </div>

                {/* Edit Button */}
                <button
                    type="button"
                    onClick={onEdit}
                    className={cn(
                        "flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium",
                        "text-white/90 hover:text-white",
                        "bg-white/15 hover:bg-white/25 backdrop-blur-sm",
                        "border border-white/20 hover:border-white/30",
                        "transition-all duration-200",
                        "focus:outline-none focus-visible:ring-2 focus-visible:ring-white/50"
                    )}
                >
                    <Pencil className="w-3.5 h-3.5"/>
                    Editar
                </button>
            </div>
        </div>
    );
}
