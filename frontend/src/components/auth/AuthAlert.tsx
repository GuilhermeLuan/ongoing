"use client";

import {cn} from "@/lib/utils";
import {AlertCircle, AlertTriangle} from "lucide-react";
import {forwardRef, HTMLAttributes} from "react";

interface AuthAlertProps extends HTMLAttributes<HTMLDivElement> {
    message: string;
    variant?: "error" | "warning";
}

const AuthAlert = forwardRef<HTMLDivElement, AuthAlertProps>(
    ({message, variant = "error", className, ...props}, ref) => {
        return (
            <div
                ref={ref}
                role="alert"
                className={cn(
                    "flex items-center gap-3 rounded-lg border px-4 py-3 text-sm animate-fadeIn",
                    {
                        "bg-red-50 border-red-200 text-red-700": variant === "error",
                        "bg-amber-50 border-amber-200 text-amber-700": variant === "warning",
                    },
                    className
                )}
                {...props}
            >
                {variant === "error" ? (
                    <AlertCircle size={18} className="flex-shrink-0"/>
                ) : (
                    <AlertTriangle size={18} className="flex-shrink-0"/>
                )}
                <p>{message}</p>
            </div>
        );
    }
);

AuthAlert.displayName = "AuthAlert";

export {AuthAlert};
