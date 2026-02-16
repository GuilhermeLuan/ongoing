"use client";

import {useEffect} from "react";
import {createPortal} from "react-dom";
import {cn} from "@/lib/utils";

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
    size?: "sm" | "md" | "lg";
}

const sizeClasses: Record<NonNullable<ModalProps["size"]>, string> = {
    sm: "max-w-md",
    md: "max-w-2xl",
    lg: "max-w-4xl",
};

export function Modal({
                          isOpen,
                          onClose,
                          title,
                          children,
                          size = "md",
                      }: ModalProps) {
    useEffect(() => {
        if (!isOpen) {
            return;
        }

        const handleEscape = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onClose();
            }
        };

        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";

        document.addEventListener("keydown", handleEscape);

        return () => {
            document.body.style.overflow = previousOverflow;
            document.removeEventListener("keydown", handleEscape);
        };
    }, [isOpen, onClose]);

    if (!isOpen || typeof window === "undefined") {
        return null;
    }

    return createPortal(
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            role="dialog"
            aria-modal="true"
            aria-label={title}
        >
            <button
                type="button"
                className="absolute inset-0 bg-neutral-900/50 animate-fadeIn"
                onClick={onClose}
                aria-label="Fechar modal"
            />

            <div
                className={cn(
                    "relative w-full rounded-xl bg-white shadow-elevated border border-neutral-100 animate-fadeInUp",
                    sizeClasses[size]
                )}
            >
                <div className="flex items-center justify-between px-6 py-4 border-b border-neutral-100">
                    <h2 className="text-lg font-semibold text-neutral-900">{title}</h2>
                    <button
                        type="button"
                        className="p-2 rounded-lg text-neutral-500 hover:bg-neutral-100 hover:text-neutral-700"
                        onClick={onClose}
                        aria-label="Fechar"
                    >
                        <svg
                            className="w-5 h-5"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M6 18L18 6M6 6l12 12"
                            />
                        </svg>
                    </button>
                </div>

                <div className="p-6">{children}</div>
            </div>
        </div>,
        document.body
    );
}
