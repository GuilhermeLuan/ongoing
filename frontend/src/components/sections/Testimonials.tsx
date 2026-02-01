"use client";

import { Container, GradientText } from "@/components/ui";
import { TestimonialCard } from "@/components/shared";
import { useScrollAnimation } from "@/hooks/useScrollAnimation";
import { cn } from "@/lib/utils";

const testimonials = [
  {
    name: "Carolina Silva",
    role: "Designer de Produto",
    content:
      "O Ongoing mudou completamente como eu gerencio minhas assinaturas. Descobri que estava pagando R$ 180/mês em serviços que nem usava mais!",
    rating: 5,
    initials: "CS",
    avatarColor: "bg-primary-500",
  },
  {
    name: "Rafael Mendes",
    role: "Desenvolvedor",
    content:
      "Muito mais do que um simples tracker. Os alertas via WhatsApp são perfeitos, nunca mais fui pego de surpresa por uma renovação.",
    rating: 5,
    initials: "RM",
    avatarColor: "bg-accent-500",
  },
  {
    name: "Juliana Costa",
    role: "Empreendedora",
    content:
      "Interface linda e fácil de usar. Consegui organizar as assinaturas da minha empresa e do meu uso pessoal em minutos. Vale cada centavo!",
    rating: 5,
    initials: "JC",
    avatarColor: "bg-blue-500",
  },
];

export function Testimonials() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section id="testimonials" className="py-20 bg-neutral-50">
      <Container>
        <div ref={ref} className="text-center mb-16">
          <h2
            className={cn(
              "font-display text-3xl md:text-4xl font-bold text-neutral-900 mb-4 opacity-0",
              isVisible && "animate-fadeInUp"
            )}
          >
            Amado por quem usa{" "}
            <GradientText>todos os dias</GradientText>
          </h2>
          <p
            className={cn(
              "text-lg text-neutral-600 max-w-2xl mx-auto opacity-0",
              isVisible && "animate-fadeInUp animation-delay-100"
            )}
          >
            Veja o que nossos usuários estão dizendo sobre como o Ongoing
            ajudou a economizar tempo e dinheiro.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {testimonials.map((testimonial, index) => (
            <div
              key={testimonial.name}
              className={cn("opacity-0", isVisible && "animate-fadeInUp")}
              style={{
                animationDelay: isVisible ? `${(index + 2) * 100}ms` : "0ms",
              }}
            >
              <TestimonialCard {...testimonial} />
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
