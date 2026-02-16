# Onboarding Wizard — Design Document

**Data:** 2026-02-16
**Status:** Aprovado
**Escopo:** Fluxo de onboarding para novos usuários após registro

---

## Visão Geral

Wizard de 4 passos que aparece imediatamente após o registro, guiando o usuário a configurar sua primeira assinatura e
conhecer o dashboard antes de começar a usar o app.

**Gatilho:** Após registro bem-sucedido (redirecionamento para `/onboarding` em vez de `/dashboard`).
**Frequência:** Uma única vez. Controlado por flag `onboardingCompleted` no backend.

---

## Estrutura Geral

**Rota:** `/onboarding` — página dedicada, fora do layout do dashboard (sem sidebar, sem AppHeader).

**Layout:** Tela cheia, centralizada, com progress bar no topo e logo. Usa o design system existente (cores, fontes,
sombras, componentes UI).

```
┌─────────────────────────────────────────────────────────┐
│  Logo                                                   │
│                                                         │
│  ━━━━━━━━●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  Bem-vindo    Primeira sub    Tour    Pronto!            │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │                                                 │    │
│  │          Conteúdo do passo atual                 │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│                                    [Botão de ação →]    │
└─────────────────────────────────────────────────────────┘
```

**Navegação:**

- Fluxo linear (sem botão "Voltar")
- Transições animadas entre passos (slide/fade)
- Guard de rota: se `onboardingCompleted === true`, redireciona para `/dashboard`

**Persistência:**

- Estado do wizard mantido em state local (React)
- Ao completar passo 4, PATCH `/api/v1/users/me` com `onboardingCompleted: true`
- Se recarregar antes de completar, recomeça do passo 1

---

## Passo 1 — Boas-vindas

**Objetivo:** Criar conexão e gerar expectativa sobre o que vem a seguir.

```
┌─────────────────────────────────────────────────────────┐
│  Logo                                                   │
│  ━━●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                                         │
│                    👋                                    │
│                                                         │
│          Bem-vindo ao Ongoing, {nome}!                  │
│                                                         │
│     Vamos configurar tudo pra você em poucos passos.    │
│     Em menos de 2 minutos você terá:                    │
│                                                         │
│     ✓  Sua primeira assinatura cadastrada               │
│     ✓  Visão completa dos seus gastos                   │
│     ✓  Controle dos próximos vencimentos                │
│                                                         │
│          ┌─────────────────────────────┐                │
│          │  Blobs decorativos animados │                │
│          │  (reutiliza pattern Hero)   │                │
│          └─────────────────────────────┘                │
│                                                         │
│                              [Vamos começar →]          │
└─────────────────────────────────────────────────────────┘
```

**Implementação:**

- Nome do usuário vem do `AuthContext`
- Headline com `GradientText` (verde → roxo, mesmo da landing)
- 3 checkmarks com cor `primary` (verde), animação staggered de entrada
- Blobs decorativos animados no fundo (reutiliza pattern da Hero section)
- Botão "Vamos começar" — `Button` primary, tamanho `lg`
- Tudo entra com `fadeInUp`

---

## Passo 2 — Adicionar Primeira Assinatura

**Objetivo:** Usuário sai com dados reais no dashboard. Sugestões reduzem fricção.

```
┌─────────────────────────────────────────────────────────┐
│  Logo                                                   │
│  ━━━━━━━━━━━━●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                                         │
│       Adicione sua primeira assinatura                  │
│       Escolha um serviço popular ou preencha manualmente│
│                                                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ 🔴       │ │ 🟢       │ │ 🔵       │ │ 🟣       │   │
│  │ Netflix  │ │ Spotify  │ │ Disney+  │ │ HBO Max  │   │
│  │ R$55,90  │ │ R$21,90  │ │ R$33,90  │ │ R$34,90  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ 🟡       │ │ 🔵       │ │ 🟠       │ │ 🟢       │   │
│  │ ChatGPT  │ │ iCloud   │ │ YouTube  │ │ Xbox GP  │   │
│  │ R$100    │ │ R$3,50   │ │ R$24,90  │ │ R$44,99  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│                                                         │
│  ── ou ──────────────────────────────────────────────   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Nome:       [                            ]     │    │
│  │  Valor:      [R$         ]  Ciclo: [Mensal ▼]   │    │
│  │  Categoria:  [Selecione ▼]                      │    │
│  │  Vencimento: [dd/mm/aaaa  📅]                   │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│                              [Adicionar e continuar →]  │
└─────────────────────────────────────────────────────────┘
```

**Implementação:**

### Cards de sugestão

- Grid 4x2 com serviços populares (dados hardcoded baseados no mock-data)
- Cada card: quadrado colorido da marca, nome, valor sugerido
- Ao clicar: pré-preenche nome, valor, categoria e cor no formulário
- Card selecionado: borda `primary` + `shadow-glow`

### Serviços sugeridos

| Serviço         | Valor     | Categoria       | Cor     |
|-----------------|-----------|-----------------|---------|
| Netflix         | R$ 55,90  | VIDEO_STREAMING | #E50914 |
| Spotify         | R$ 21,90  | MUSIC           | #1DB954 |
| Disney+         | R$ 33,90  | VIDEO_STREAMING | #1CE783 |
| HBO Max         | R$ 34,90  | VIDEO_STREAMING | #8B5CF6 |
| ChatGPT Plus    | R$ 100,00 | PRODUCTIVITY    | #10A37F |
| iCloud          | R$ 3,50   | CLOUD_STORAGE   | #4285F4 |
| YouTube Premium | R$ 24,90  | VIDEO_STREAMING | #FF0000 |
| Xbox Game Pass  | R$ 44,99  | GAMING          | #107C10 |

### Formulário

- Reutiliza componentes `Input` e `Select` existentes
- Campos: nome, valor (R$), ciclo (Mensal/Anual/Semanal), categoria, data de vencimento
- Se sugestão selecionada: só vencimento é obrigatório preencher
- Se manual: todos os campos obrigatórios
- **Sem opção de pular** — obrigatório adicionar pelo menos uma
- Ao submeter: `POST /api/v1/subscriptions` — salva de verdade no backend
- Dados da sub criada ficam no state do wizard (usados no passo 4)

---

## Passo 3 — Tour Animado do Dashboard

**Objetivo:** Preparar o usuário pra interface, mostrando as 3 áreas principais.

```
┌─────────────────────────────────────────────────────────┐
│  Logo                                                   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━●━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                                         │
│       Conheça seu painel de controle                    │
│       Tudo que você precisa, em um só lugar             │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │                                                 │    │
│  │   ┌────────────────────────────────────────┐    │    │
│  │   │ ┌────┐ ┌────┐ ┌────┐ ┌────┐           │    │    │
│  │   │ │R$  │ │ 12 │ │ 3  │ │ 2d │  ← ✨     │    │    │
│  │   │ └────┘ └────┘ └────┘ └────┘  highlight │    │    │
│  │   │                               ativo    │    │    │
│  │   │  ┌──────────────┐ ┌────────┐           │    │    │
│  │   │  │  Próx. venc. │ │Categ.  │           │    │    │
│  │   │  │  ──────────  │ │ ██ 40% │           │    │    │
│  │   │  │  ──────────  │ │ ██ 25% │           │    │    │
│  │   │  └──────────────┘ └────────┘           │    │    │
│  │   └────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  │   ● Seus gastos e assinaturas em tempo real     │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│       ○ ● ○                          [Próximo →]        │
└─────────────────────────────────────────────────────────┘
```

**Implementação:**

### Mockup do dashboard

- Versão simplificada/estilizada do dashboard real (ilustração esquemática)
- Reutiliza o padrão do componente `DashboardPreview` da landing page
- Cores e proporções do app real

### 3 sub-slides com highlight animado

O mockup fica fixo. Um highlight (borda brilhante + pulse) se move entre as áreas:

| Sub-slide | Área destacada        | Texto                                              |
|-----------|-----------------------|----------------------------------------------------|
| 1         | 4 StatCards           | "Seus gastos e assinaturas em tempo real"          |
| 2         | Lista próximos venc.  | "Nunca perca um vencimento — veja o que vem aí"    |
| 3         | Gráfico de categorias | "Entenda pra onde seu dinheiro vai, por categoria" |

### Navegação dos sub-slides

- 3 dots (●○○) embaixo do mockup
- Avança automaticamente a cada 4 segundos
- Usuário pode clicar nos dots ou no "Próximo" pra avançar manualmente
- Highlight entra com `scaleIn` + `shadow-glow`
- Texto entra com `fadeInUp`
- Botão "Próximo" (pro passo 4) só habilita quando passou pelos 3 sub-slides

---

## Passo 4 — Sucesso

**Objetivo:** Celebrar, mostrar resumo do que foi feito e direcionar pro dashboard.

```
┌─────────────────────────────────────────────────────────┐
│  Logo                                                   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━●    │
│                                                         │
│                    🎉                                    │
│                 (confetti burst)                         │
│                                                         │
│            Tudo pronto, {nome}!                         │
│                                                         │
│       Seu Ongoing está configurado e funcionando.       │
│                                                         │
│       ┌─────────────────────────────────────┐           │
│       │                                     │           │
│       │  ✓  Netflix adicionada              │           │
│       │     R$ 55,90/mês · Vence em 15/03   │           │
│       │                                     │           │
│       └─────────────────────────────────────┘           │
│                                                         │
│       Próximos passos:                                  │
│                                                         │
│       📋  Adicione mais assinaturas pelo dashboard      │
│       🔍  Use filtros pra encontrar qualquer uma        │
│       📅  Fique de olho nos vencimentos da semana       │
│                                                         │
│                  [Ir para o Dashboard →]                 │
└─────────────────────────────────────────────────────────┘
```

**Implementação:**

### Confetti

- Animação CSS pura (partículas coloridas caindo ~2 segundos)
- Cores: `primary` (verde), `accent` (roxo), `amber`
- Dispara ao montar o componente

### Resumo

- Headline "Tudo pronto, {nome}!" com `GradientText`, entra com `scaleIn`
- Card de resumo mostra a assinatura criada no passo 2 (dados do state do wizard)
- Usa visual do `SubscriptionCard` compact (nome, valor/ciclo, vencimento)

### Próximos passos

- 3 dicas curtas com ícones
- Animação staggered `fadeInUp`
- Servem como ponte pro uso real

### Botão "Ir para o Dashboard"

- `Button` primary, tamanho `lg`
- Ao clicar:
    1. `PATCH /api/v1/users/me` com `{ onboardingCompleted: true }`
    2. Redireciona para `/dashboard`

---

## Arquitetura de Componentes

```
frontend/src/
├── app/
│   └── (onboarding)/
│       ├── layout.tsx              # Layout limpo (só logo + progress bar)
│       └── onboarding/
│           └── page.tsx            # Orquestra os 4 passos
│
├── components/
│   └── onboarding/
│       ├── OnboardingWizard.tsx    # State machine dos passos
│       ├── ProgressBar.tsx         # Barra de progresso com labels
│       ├── StepWelcome.tsx         # Passo 1
│       ├── StepAddSubscription.tsx # Passo 2
│       ├── StepDashboardTour.tsx   # Passo 3
│       ├── StepSuccess.tsx         # Passo 4
│       ├── ServiceSuggestionCard.tsx # Card de sugestão (passo 2)
│       ├── DashboardMockup.tsx     # Mockup animado (passo 3)
│       └── ConfettiAnimation.tsx   # Confetti CSS (passo 4)
```

## Mudanças no Backend

1. **Campo `onboardingCompleted`** na entidade `User` (default: `false`)
2. **Migration Flyway** adicionando a coluna
3. **Endpoint PATCH** `/api/v1/users/me` para atualizar o campo
4. **Incluir `onboardingCompleted`** na resposta de login/refresh token (para o frontend saber se deve redirecionar)

## Mudanças no Frontend (existente)

1. **RegisterForm:** Após registro, redirecionar para `/onboarding` em vez de `/dashboard`
2. **AuthContext:** Ler `onboardingCompleted` do token/resposta de auth
3. **Guard de rota:** Se `onboardingCompleted === true` e rota é `/onboarding`, redirecionar para `/dashboard`

---

## Fluxo Completo

```
Usuário acessa /register
        │
        ▼
Preenche formulário → POST /api/v1/auth/register
        │
        ▼
Registro OK → Redireciona para /onboarding
        │
        ▼
Passo 1: Boas-vindas → [Vamos começar]
        │
        ▼
Passo 2: Adicionar sub → Escolhe/preenche → POST /api/v1/subscriptions
        │
        ▼
Passo 3: Tour dashboard → Vê 3 highlights animados
        │
        ▼
Passo 4: Sucesso → [Ir para o Dashboard]
        │
        ▼
PATCH /api/v1/users/me { onboardingCompleted: true }
        │
        ▼
Redireciona para /dashboard (com dados reais!)
```
