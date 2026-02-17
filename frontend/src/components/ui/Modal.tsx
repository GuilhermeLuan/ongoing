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

        const scrollY = window.scrollY;
        const previousBodyStyles = {
            position: document.body.style.position,
            top: document.body.style.top,
            left: document.body.style.left,
            right: document.body.style.right,
            width: document.body.style.width,
        };

        document.body.style.position = "fixed";
        document.body.style.top = `-${scrollY}px`;
        document.body.style.left = "0";
        document.body.style.right = "0";
        document.body.style.width = "100%";

        document.addEventListener("keydown", handleEscape);

        return () => {
            document.body.style.position = previousBodyStyles.position;
            document.body.style.top = previousBodyStyles.top;
            document.body.style.left = previousBodyStyles.left;
            document.body.style.right = previousBodyStyles.right;
            document.body.style.width = previousBodyStyles.width;
            window.scrollTo(0, scrollY);
            document.removeEventListener("keydown", handleEscape);
        };
    }, [isOpen, onClose]);

    if (!isOpen || typeof window === "undefined") {
        return null;
    }

    return createPortal(
        <div
            className="fixed inset-0 z-[70] flex items-center justify-center p-4"
            role="dialog"
            aria-modal="true"
            aria-label={title}
            style={{
                paddingTop: "max(1rem, env(safe-area-inset-top))",
                paddingRight: "max(1rem, env(safe-area-inset-right))",
                paddingBottom: "max(1rem, env(safe-area-inset-bottom))",
                paddingLeft: "max(1rem, env(safe-area-inset-left))",
            }}
        >
            <button
                type="button"
                className="absolute inset-0 bg-neutral-900/45 animate-fadeIn"
                onClick={onClose}
                aria-label="Fechar modal"
            />

            <div
                className={cn(
                    "relative flex max-h-[calc(100dvh-2rem)] w-full flex-col rounded-xl border border-neutral-100 bg-white shadow-elevated will-change-transform animate-fadeIn md:animate-fadeInUp",
                    sizeClasses[size]
                )}
            >
                <div className="flex items-center justify-between border-b border-neutral-100 px-6 py-4">
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

                <div className="overflow-y-auto p-6 [overscroll-behavior:contain]">
                    {children}
                </div>
            </div>
        </div>,
        document.body
    );
}
