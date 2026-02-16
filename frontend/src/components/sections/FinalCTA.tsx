"use client";

import {Button, Container, GradientText} from "@/components/ui";
import {useScrollAnimation} from "@/hooks/useScrollAnimation";
import {cn} from "@/lib/utils";
import {ArrowRight, CheckCircle} from "lucide-react";

const benefits = [
  "Grátis para sempre no plano básico",
  "Sem cartão de crédito",
  "Cancele quando quiser",
];

export function FinalCTA() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section className="py-20 bg-white relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-gradient-to-r from-primary-100 to-accent-100 rounded-full blur-3xl opacity-50" />
      </div>

      <Container size="md">
        <div
          ref={ref}
          className={cn(
            "text-center opacity-0",
            isVisible && "animate-fadeInUp"
          )}
        >
          <h2 className="font-display text-3xl md:text-4xl lg:text-5xl font-bold text-neutral-900 mb-6">
            Pronto para ter suas assinaturas{" "}
            <GradientText>sob controle</GradientText>?
          </h2>

          <p className="text-lg md:text-xl text-neutral-600 mb-8 max-w-2xl mx-auto">
            Junte-se a milhares de brasileiros que já economizam tempo e
            dinheiro com o Ongoing.
          </p>

            <div className="flex justify-center mb-8">
            <Button size="lg" className="group">
              Criar conta grátis
              <ArrowRight
                size={20}
                className="ml-2 transition-transform group-hover:translate-x-1"
              />
            </Button>
          </div>

          <div className="flex flex-wrap justify-center gap-6">
            {benefits.map((benefit) => (
              <div
                key={benefit}
                className="flex items-center gap-2 text-neutral-600"
              >
                <CheckCircle size={18} className="text-primary-500" />
                <span>{benefit}</span>
              </div>
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
