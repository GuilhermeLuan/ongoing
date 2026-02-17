import {Logo} from "@/components/shared";
import {GuestRoute} from "@/components/auth";

export default function AuthLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <GuestRoute>
            <div
                className="min-h-[100dvh] flex flex-col lg:flex-row bg-gradient-to-br from-primary-500 via-primary-600 to-accent-500">
            {/* Branding Panel */}
            <div
                className="relative flex items-center justify-center bg-transparent lg:w-1/2 px-8 py-12 lg:py-0 overflow-hidden">
                {/* Decorative circles */}
                <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-white/10 blur-3xl"/>
                <div
                    className="absolute bottom-[-15%] right-[-10%] w-[40%] h-[40%] rounded-full bg-accent-400/20 blur-3xl"/>
                <div className="absolute top-[30%] right-[15%] w-32 h-32 rounded-full border border-white/10"/>
                <div className="absolute bottom-[20%] left-[10%] w-20 h-20 rounded-full border border-white/10"/>

                {/* Content */}
                <div className="relative z-10 text-center max-w-md">
                    <div className="flex justify-center mb-8">
                        <Logo size="lg" className="[&_span]:text-white"/>
                    </div>
                    <h2 className="text-2xl lg:text-3xl font-display font-bold text-white leading-tight">
                        Gerencie suas assinaturas de forma inteligente
                    </h2>
                    <p className="mt-4 text-white/70 text-base lg:text-lg">
                        Controle gastos, receba alertas e economize dinheiro.
                    </p>
                </div>
            </div>

            {/* Form Panel */}
            <div className="flex-1 flex items-center justify-center bg-neutral-50 px-6 py-12 lg:px-8 lg:py-0">
                {children}
            </div>
        </div>
        </GuestRoute>
    );
}
