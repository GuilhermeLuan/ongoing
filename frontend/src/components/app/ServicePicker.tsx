"use client";

import {useMemo, useState} from "react";
import {Plus, Search} from "lucide-react";
import {type PopularService, popularServices} from "@/features/subscriptions";
import {cn} from "@/lib/utils";

interface ServicePickerProps {
    onSelectService: (service: PopularService) => void;
    onCreateCustom: (name: string) => void;
}

export function ServicePicker({onSelectService, onCreateCustom}: ServicePickerProps) {
    const [searchQuery, setSearchQuery] = useState("");

    const filteredServices = useMemo(() => {
        if (!searchQuery.trim()) {
            return popularServices;
        }

        const query = searchQuery.toLowerCase().trim();
        return popularServices.filter((service) =>
            service.name.toLowerCase().includes(query)
        );
    }, [searchQuery]);

    const showCustomCard = searchQuery.trim().length > 0 && filteredServices.length === 0;

    return (
        <div className="space-y-6">
            {/* Search Bar */}
            <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-neutral-400"/>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Buscar serviço..."
                    className={cn(
                        "w-full pl-10 pr-4 py-3 rounded-xl border border-neutral-200",
                        "bg-white text-neutral-900 placeholder:text-neutral-400",
                        "focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500",
                        "transition-all duration-200",
                        "text-base font-medium"
                    )}
                />
            </div>

            {/* Section Heading */}
            <div>
                <h3 className="text-sm font-semibold text-neutral-500 uppercase tracking-wider mb-4">
                    {searchQuery.trim() ? "Resultados" : "Serviços populares"}
                </h3>

                {/* Services Grid */}
                <div
                    className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
                    {filteredServices.map((service) => (
                        <ServiceCard
                            key={service.name}
                            service={service}
                            onClick={() => onSelectService(service)}
                        />
                    ))}

                    {/* Custom Service Card */}
                    {showCustomCard && (
                        <button
                            onClick={() => onCreateCustom(searchQuery.trim())}
                            className={cn(
                                "group relative flex flex-col items-center justify-center gap-3 p-4 rounded-xl",
                                "border-2 border-dashed border-neutral-300 bg-neutral-50/50",
                                "hover:border-primary-400 hover:bg-primary-50/50",
                                "transition-all duration-200",
                                "min-h-[120px]"
                            )}
                        >
                            <div
                                className={cn(
                                    "w-12 h-12 rounded-full flex items-center justify-center",
                                    "bg-neutral-200 text-neutral-500",
                                    "group-hover:bg-primary-500 group-hover:text-white",
                                    "transition-all duration-200"
                                )}
                            >
                                <Plus className="w-6 h-6"/>
                            </div>
                            <div className="text-center">
                                <p className="text-sm font-medium text-neutral-700 group-hover:text-primary-700 transition-colors">
                                    Criar &ldquo;{searchQuery.trim()}&rdquo;
                                </p>
                            </div>
                        </button>
                    )}
                </div>
            </div>

            {/* Custom Scrollbar Styles */}
            <style jsx>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: #d4d4d8;
          border-radius: 3px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: #a1a1aa;
        }
      `}</style>
        </div>
    );
}

interface ServiceCardProps {
    service: PopularService;
    onClick: () => void;
}

function ServiceCard({service, onClick}: ServiceCardProps) {
    const [imageError, setImageError] = useState(false);

    // Generate initials from service name (first 2 letters)
    const initials = service.name
        .split(" ")
        .map((word) => word[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);

    return (
        <button
            onClick={onClick}
            className={cn(
                "group relative flex flex-col items-center gap-3 p-4 rounded-xl",
                "border border-neutral-200 bg-white",
                "hover:border-primary-200 hover:bg-primary-50/50 hover:shadow-md",
                "transition-all duration-200",
                "min-h-[120px]",
                "focus:outline-none focus:ring-2 focus:ring-primary-500/20"
            )}
        >
            {/* Logo */}
            <div
                className="w-12 h-12 rounded-full overflow-hidden flex items-center justify-center bg-gradient-to-br from-primary-100 to-accent-100 shrink-0">
                {!imageError ? (
                    <img
                        src={service.logoUrl}
                        alt={service.name}
                        className="w-10 h-10 object-contain"
                        onError={() => setImageError(true)}
                    />
                ) : (
                    <span className="text-sm font-bold text-primary-700">{initials}</span>
                )}
            </div>

            {/* Service Name */}
            <p className="text-sm font-semibold text-neutral-800 text-center leading-tight group-hover:text-primary-700 transition-colors">
                {service.name}
            </p>

            {/* Hover Effect Indicator */}
            <div
                className={cn(
                    "absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100",
                    "bg-gradient-to-br from-primary-500/5 to-accent-500/5",
                    "transition-opacity duration-200",
                    "pointer-events-none"
                )}
            />
        </button>
    );
}
