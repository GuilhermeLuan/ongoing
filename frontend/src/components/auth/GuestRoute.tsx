"use client";

import {useEffect} from "react";
import {useRouter} from "next/navigation";
import {useAuth} from "@/features/auth";
import {Logo} from "@/components/shared";

export function GuestRoute({children}: { children: React.ReactNode }) {
    const {isInitialized, user} = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (isInitialized && user) {
            router.replace("/dashboard");
        }
    }, [isInitialized, user, router]);

    if (!isInitialized || user) {
        return (
            <div className="min-h-[100dvh] bg-neutral-50 flex items-center justify-center">
                <div className="flex flex-col items-center gap-4">
                    <Logo/>
                    <div
                        className="w-8 h-8 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"/>
                </div>
            </div>
        );
    }

    return <>{children}</>;
}
