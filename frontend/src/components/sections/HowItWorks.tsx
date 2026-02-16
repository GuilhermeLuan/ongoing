"use client";

import {Container, GradientText} from "@/components/ui";
import {StepCard} from "@/components/shared";
import {useScrollAnimation} from "@/hooks/useScrollAnimation";
import {cn} from "@/lib/utils";
import {BellRing, ListPlus, UserPlus} from "lucide-react";

const steps = [
  {
    step: 1,
    icon: UserPlus,
    title: "Crie sua conta",
    description:
      "Cadastro rápido e gratuito. Em menos de 1 minuto você já está dentro.",
  },
  {
    step: 2,
    icon: ListPlus,
    title: "Adicione assinaturas",
    description:
        "Cadastre suas assinaturas em poucos cliques. Rápido, simples e organizado.",
  },
  {
    step: 3,
    icon: BellRing,
    title: "Receba alertas",
    description:
      "Pronto! Agora você será avisado antes de cada cobrança. Simples assim.",
  },
];

export function HowItWorks() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section id="how-it-works" className="py-20 bg-neutral-50">
      <Container>
        <div ref={ref} className="text-center mb-16">
          <h2
            className={cn(
              "font-display text-3xl md:text-4xl font-bold text-neutral-900 mb-4 opacity-0",
              isVisible && "animate-fadeInUp"
            )}
          >
            Como <GradientText>funciona</GradientText>
          </h2>
          <p
            className={cn(
              "text-lg text-neutral-600 max-w-2xl mx-auto opacity-0",
              isVisible && "animate-fadeInUp animation-delay-100"
            )}
          >
            Três passos simples para você nunca mais perder o controle das suas
            assinaturas.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-12 relative">
          {/* Connection line (desktop only) */}
          <div className="hidden md:block absolute top-8 left-1/4 right-1/4 h-0.5 bg-gradient-to-r from-primary-200 via-primary-400 to-primary-200" />

          {steps.map((step, index) => (
            <div
              key={step.step}
              className={cn(
                "opacity-0",
                isVisible && "animate-fadeInUp"
              )}
              style={{
                animationDelay: isVisible ? `${(index + 2) * 150}ms` : "0ms",
              }}
            >
              <StepCard {...step} />
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
