"use client";

import {Container, GradientText} from "@/components/ui";
import {PricingCard} from "@/components/shared";
import {useScrollAnimation} from "@/hooks/useScrollAnimation";
import {cn} from "@/lib/utils";

const plans = [
  {
    name: "Grátis",
    price: "Grátis",
    description: "Perfeito para começar",
    features: [
      "Até 5 assinaturas",
      "Alertas por email",
      "Dashboard básico",
      "Suporte por email",
    ],
  },
  {
    name: "Pro",
    price: "R$ 14,90",
    description: "Para quem leva a sério",
    features: [
      "Assinaturas ilimitadas",
        "Alertas personalizáveis",
      "Dashboard completo",
      "Relatórios mensais",
        "Exportação de dados",
      "Suporte prioritário",
    ],
    highlighted: true,
    badge: "Mais popular",
  },
];

export function Pricing() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section id="pricing" className="py-20 bg-white">
      <Container>
        <div ref={ref} className="text-center mb-16">
          <h2
            className={cn(
              "font-display text-3xl md:text-4xl font-bold text-neutral-900 mb-4 opacity-0",
              isVisible && "animate-fadeInUp"
            )}
          >
            Planos que cabem no seu{" "}
            <GradientText>bolso</GradientText>
          </h2>
          <p
            className={cn(
              "text-lg text-neutral-600 max-w-2xl mx-auto opacity-0",
              isVisible && "animate-fadeInUp animation-delay-100"
            )}
          >
            Comece grátis e faça upgrade quando precisar. Sem surpresas, sem
            taxas escondidas.
          </p>
        </div>

          <div className="grid md:grid-cols-2 gap-8 items-center max-w-3xl mx-auto">
          {plans.map((plan, index) => (
            <div
              key={plan.name}
              className={cn(
                "opacity-0",
                isVisible && "animate-fadeInUp"
              )}
              style={{
                animationDelay: isVisible ? `${(index + 2) * 100}ms` : "0ms",
              }}
            >
              <PricingCard {...plan} />
            </div>
          ))}
        </div>

        <p className="text-center mt-12 text-neutral-500">
          Todos os planos incluem 7 dias de teste grátis do Pro.{" "}
          <a href="#" className="text-primary-600 hover:underline">
            Ver comparação completa
          </a>
        </p>
      </Container>
    </section>
  );
}
