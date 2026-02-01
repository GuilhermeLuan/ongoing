# Plano: Landing Page Ongoing

## Resumo
Landing page Next.js para o **Ongoing** - subscription tracker brasileiro com estética "Fintech Brasileira Amigável" (inspirada no AbacatePay).

**Público-alvo:** Usuários finais que querem gerenciar assinaturas pessoais.

---

## Stack & Dependências

```bash
npx create-next-app@latest . --typescript --tailwind --eslint --app --src-dir --import-alias "@/*"
npm install lucide-react clsx tailwind-merge
npm install -D @tailwindcss/typography
```

---

## Estrutura de Arquivos

```
src/
├── app/
│   ├── layout.tsx          # Fontes (Plus Jakarta Sans, Inter, JetBrains Mono)
│   ├── page.tsx            # Página principal
│   └── globals.css         # CSS variables + Tailwind customizado
│
├── components/
│   ├── ui/                 # Button, Card, Badge, Container, GradientText
│   ├── sections/           # Hero, Features, HowItWorks, Pricing, Testimonials, FinalCTA
│   ├── layout/             # Header, Footer
│   └── shared/             # Logo, FeatureCard, PricingCard, TestimonialCard, StepCard, DashboardPreview
│
├── lib/
│   └── utils.ts            # cn() helper
│
└── hooks/
    └── useScrollAnimation.ts
```

---

## Seções da Landing Page

| Seção | Descrição |
|-------|-----------|
| **Hero** | Headline impactante + CTA + Preview do dashboard |
| **Features** | 6 features com ícones Lucide |
| **Como Funciona** | 3 passos ilustrados |
| **Preços** | Grátis, Pro (R$14,90), Business (R$49,90) |
| **Testimonials** | 3 depoimentos de usuários brasileiros |
| **CTA Final** | Call to action de conversão |
| **Footer** | Links, redes sociais, copyright |

---

## Design System

### Cores
- **Primária (Verde Abacate):** `#22c55e` (principal), `#16a34a` (hover)
- **Accent (Roxo Premium):** `#8b5cf6`
- **Neutros:** Escala de `#fafafa` a `#171717`

### Tipografia
- **Display:** Plus Jakarta Sans (headlines)
- **Body:** Inter (texto corrido)
- **Mono:** JetBrains Mono (valores monetários)

### Componentes
- Border radius arredondados (8-12px)
- Sombras sutis (`shadow-soft`, `shadow-medium`, `shadow-elevated`)
- Gradientes sutis para headers e accents
- Hover states com `translateY(-2px)` e glow effects

### Animações
- `fadeIn`, `fadeInUp`, `scaleIn`, `slideInLeft/Right`
- `float` (para elementos decorativos)
- Delays escalonados para stagger effects

---

## Ordem de Implementação

1. **Setup** - Criar projeto Next.js + instalar deps
2. **Design System** - `tailwind.config.ts`, `globals.css`, `layout.tsx`
3. **Componentes UI** - Button, Card, Badge, Container, GradientText
4. **Componentes Shared** - Logo, FeatureCard, PricingCard, etc.
5. **Layout** - Header, Footer
6. **Seções** - Hero → Features → HowItWorks → Pricing → Testimonials → FinalCTA
7. **Integração** - `page.tsx` + ajustes finais

---

## Arquivos Críticos

| Arquivo | Propósito |
|---------|-----------|
| `tailwind.config.ts` | Design tokens (cores, fontes, animações) |
| `globals.css` | CSS variables e classes utilitárias |
| `layout.tsx` | Configuração de fontes via next/font |
| `Hero.tsx` | Estabelece tom visual da página |
| `Button.tsx` | Padrão de variantes para todos os botões |

---

## Decisões de Design

- **Avatares dos Testimonials:** Placeholders com círculos coloridos + iniciais (sem imagens externas)

---

## Verificação

Para testar a implementação:

```bash
npm run dev
# Acessar http://localhost:3000
```

**Checklist de verificação:**
- [ ] Fontes carregando corretamente (Plus Jakarta Sans, Inter)
- [ ] Cores verde abacate aplicadas nos CTAs
- [ ] Animações de scroll funcionando
- [ ] Layout responsivo (testar mobile, tablet, desktop)
- [ ] Hover states nos cards e botões
- [ ] Header com blur no scroll
- [ ] Preview do dashboard com animação float
