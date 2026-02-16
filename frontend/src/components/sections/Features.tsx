"use client";

import {Container, GradientText} from "@/components/ui";
import {FeatureCard} from "@/components/shared";
import {useScrollAnimation} from "@/hooks/useScrollAnimation";
import {cn} from "@/lib/utils";
import {Bell, Calendar, PieChart, Shield,} from "lucide-react";

const features = [
  {
    icon: Bell,
    title: "Alertas Inteligentes",
    description:
      "Receba notificações antes de cada cobrança. Nunca mais seja pego de surpresa por uma renovação automática.",
  },
  {
    icon: PieChart,
    title: "Dashboard Visual",
    description:
      "Visualize todos os seus gastos com assinaturas em gráficos claros e intuitivos. Entenda para onde vai seu dinheiro.",
  },
  {
    icon: Shield,
    title: "Dados Seguros",
    description:
      "Seus dados são criptografados e nunca compartilhados. Privacidade é nossa prioridade.",
  },
  {
    icon: Calendar,
    title: "Calendário de Cobranças",
    description:
      "Veja todas as cobranças futuras em um calendário. Planeje seus gastos com antecedência.",
  },
];

export function Features() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section id="features" className="py-20 bg-white">
      <Container>
        <div ref={ref} className="text-center mb-16">
          <h2
            className={cn(
              "font-display text-3xl md:text-4xl font-bold text-neutral-900 mb-4 opacity-0",
              isVisible && "animate-fadeInUp"
            )}
          >
            Tudo que você precisa para{" "}
            <GradientText>controlar gastos</GradientText>
          </h2>
          <p
            className={cn(
              "text-lg text-neutral-600 max-w-2xl mx-auto opacity-0",
              isVisible && "animate-fadeInUp animation-delay-100"
            )}
          >
            Recursos poderosos e simples de usar. Projetado para quem quer ter
            controle total das suas assinaturas sem complicação.
          </p>
        </div>

          <div className="grid md:grid-cols-2 gap-6 max-w-4xl mx-auto">
          {features.map((feature, index) => (
            <div
              key={feature.title}
              className={cn(
                "opacity-0",
                isVisible && "animate-fadeInUp",
                isVisible && `animation-delay-${(index + 2) * 100}`
              )}
              style={{
                animationDelay: isVisible ? `${(index + 2) * 100}ms` : "0ms",
              }}
            >
              <FeatureCard {...feature} />
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
