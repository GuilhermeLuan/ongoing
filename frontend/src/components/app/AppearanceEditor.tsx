"use client";

import {useState} from "react";
import {Button, Input} from "@/components/ui";
import {ColorPicker} from "./ColorPicker";
import {cn} from "@/lib/utils";

interface AppearanceEditorProps {
    name: string;
    logoUrl?: string;
    avatarColor: string;
    onSave: (name: string, color: string) => void;
    onCancel: () => void;
}

export function AppearanceEditor({name, logoUrl, avatarColor, onSave, onCancel}: AppearanceEditorProps) {
    const [localName, setLocalName] = useState(name);
    const [localColor, setLocalColor] = useState(avatarColor);
    const [imageError, setImageError] = useState(false);

    const initials = localName
        .split(" ")
        .map((word) => word[0])
        .filter(Boolean)
        .join("")
        .toUpperCase()
        .slice(0, 2) || "??";

    const showImage = logoUrl && !imageError;

    const handleSave = () => {
        if (!localName.trim()) return;
        onSave(localName.trim(), localColor);
    };

    return (
        <div
            className={cn(
                "rounded-xl border border-neutral-200",
                "bg-gradient-to-b from-neutral-50 to-white",
                "p-6 space-y-5"
            )}
        >
            {/* Header */}
            <h4 className="text-sm font-semibold text-neutral-500 uppercase tracking-wider">
                Editar aparência
            </h4>

            {/* Logo Preview - full-width colored banner */}
            <div
                className="relative -mx-6 flex items-center justify-center py-8 transition-colors duration-300"
                style={{backgroundColor: localColor}}
            >
                <div
                    className={cn(
                        "w-20 h-20 rounded-2xl flex items-center justify-center",
                        "bg-white shadow-lg"
                    )}
                >
                    {showImage ? (
                        <img
                            src={logoUrl}
                            alt={localName}
                            className="w-12 h-12 object-contain"
                            onError={() => setImageError(true)}
                        />
                    ) : (
                        <span
                            className="text-2xl font-bold"
                            style={{color: localColor}}
                        >
                            {initials}
                        </span>
                    )}
                </div>
            </div>

            {/* Name Input */}
            <Input
                label="Nome do serviço"
                value={localName}
                onChange={(e) => setLocalName(e.target.value)}
                maxLength={255}
            />

            {/* Color Picker */}
            <ColorPicker
                label="Cor de fundo do avatar"
                value={localColor}
                onChange={setLocalColor}
            />

            {/* Actions */}
            <div className="flex justify-end gap-3 pt-1">
                <Button type="button" variant="ghost" size="sm" onClick={onCancel}>
                    Cancelar
                </Button>
                <Button type="button" size="sm" onClick={handleSave} disabled={!localName.trim()}>
                    Salvar
                </Button>
            </div>
        </div>
    );
}
