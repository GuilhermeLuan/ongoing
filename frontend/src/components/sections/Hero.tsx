"use client";

import { Container, Button, Badge, GradientText } from "@/components/ui";
import { DashboardPreview } from "@/components/shared";
import { useScrollAnimation } from "@/hooks/useScrollAnimation";
import { cn } from "@/lib/utils";
import { ArrowRight, Sparkles } from "lucide-react";

export function Hero() {
  const { ref, isVisible } = useScrollAnimation();

  return (
    <section className="relative pt-32 pb-20 overflow-hidden">
      {/* Background gradient */}
      <div
        className="absolute inset-0 -z-10"
        style={{
          background:
            "linear-gradient(180deg, #f0fdf4 0%, #ffffff 50%, #ffffff 100%)",
        }}
      />

      {/* Decorative elements */}
      <div className="absolute top-20 left-10 w-72 h-72 bg-primary-200 rounded-full blur-3xl opacity-30 animate-float" />
      <div className="absolute bottom-20 right-10 w-96 h-96 bg-accent-200 rounded-full blur-3xl opacity-20 animate-float animation-delay-300" />

      <Container>
        <div ref={ref} className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Content */}
          <div
            className={cn(
              "text-center lg:text-left opacity-0",
              isVisible && "animate-fadeInUp"
            )}
          >
            <Badge variant="primary" className="mb-6 inline-flex items-center gap-2">
              <Sparkles size={14} />
              Novo: Alertas inteligentes via WhatsApp
            </Badge>

            <h1 className="font-display text-4xl md:text-5xl lg:text-6xl font-bold text-neutral-900 mb-6 leading-tight">
              Suas assinaturas.{" "}
              <GradientText as="span">Sob controle.</GradientText>
            </h1>

            <p className="text-lg md:text-xl text-neutral-600 mb-8 max-w-xl mx-auto lg:mx-0 leading-relaxed">
              Chega de surpresas na fatura. O Ongoing reúne todas as suas
              assinaturas em um só lugar, avisa antes de cobranças e ajuda você
              a economizar.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
              <Button size="lg" className="group">
                Começar grátis
                <ArrowRight
                  size={20}
                  className="ml-2 transition-transform group-hover:translate-x-1"
                />
              </Button>
              <Button variant="outline" size="lg">
                Ver demonstração
              </Button>
            </div>

            <p className="mt-6 text-sm text-neutral-500">
              ✓ Grátis para sempre no plano básico &nbsp;·&nbsp; ✓ Sem cartão de
              crédito
            </p>
          </div>

          {/* Dashboard Preview */}
          <div
            className={cn(
              "relative opacity-0",
              isVisible && "animate-fadeInUp animation-delay-200"
            )}
          >
            <div className="absolute inset-0 bg-gradient-to-tr from-primary-500/20 to-accent-500/20 rounded-3xl blur-2xl -z-10 scale-95" />
            <DashboardPreview className="animate-float" />
          </div>
        </div>
      </Container>
    </section>
  );
}
