# Plano: Frontend do App Ongoing

## Contexto
Implementar o dashboard do app de gerenciamento de assinaturas na mesma pasta da landing page (monorepo), seguindo o design guide existente em `.claude/plans/design.md`.

**Escopo:** Dashboard + Lista de Assinaturas (sem autenticação)

---

## 1. Reorganizar estrutura com Route Groups

Mover a landing page para um route group `(marketing)` e criar o grupo `(app)` para o dashboard.

```
src/app/
├── (marketing)/
│   ├── layout.tsx        # Layout da landing (Header/Footer)
│   └── page.tsx          # Landing page atual
├── (app)/
│   ├── layout.tsx        # Layout do app (Sidebar)
│   ├── dashboard/
│   │   └── page.tsx      # Página principal
│   └── subscriptions/
│       └── page.tsx      # Lista de assinaturas
├── globals.css
└── layout.tsx            # Root layout (fonts)
```

**Arquivos a modificar:**
- `src/app/page.tsx` → mover para `src/app/(marketing)/page.tsx`
- `src/app/layout.tsx` → extrair layout de marketing para `src/app/(marketing)/layout.tsx`

---

## 2. Novos componentes UI

Criar em `src/components/ui/`:

| Componente | Descrição |
|------------|-----------|
| `Input.tsx` | Input de texto com estados focus/error |
| `Select.tsx` | Select dropdown estilizado |
| `Avatar.tsx` | Avatar circular para usuário/logos |
| `Skeleton.tsx` | Loading placeholder |

---

## 3. Layout do App

Criar em `src/components/app/`:

| Componente | Descrição |
|------------|-----------|
| `Sidebar.tsx` | Navegação lateral (240px) com links e logo |
| `AppHeader.tsx` | Header com search e perfil do usuário |
| `StatCard.tsx` | Card de métrica (gasto mensal, total assinaturas, etc.) |
| `SubscriptionCard.tsx` | Card de assinatura individual |
| `SubscriptionList.tsx` | Lista de assinaturas com filtros |

---

## 4. Páginas

### Dashboard (`/dashboard`)
- Saudação personalizada
- 4 StatCards: Gasto Mensal, Total Assinaturas, Vencendo Esta Semana, Próximo Vencimento
- Lista de próximos vencimentos (5 itens)
- Gráfico de gastos por categoria (pode ser placeholder inicialmente)

### Lista de Assinaturas (`/subscriptions`)
- Header com título e botão "Adicionar"
- Filtros: busca por nome, categoria, status
- Grid/Lista de SubscriptionCards
- Estados: loading, empty, error

---

## 5. Dados Mock

Criar em `src/lib/mock-data.ts`:

```typescript
// Assinaturas de exemplo
export const mockSubscriptions = [
  { id: 1, name: "Netflix", category: "VIDEO_STREAMING", price: 55.90, billingCycle: "MONTHLY", nextBilling: "2024-02-15", status: "ACTIVE" },
  { id: 2, name: "Spotify", category: "MUSIC", price: 21.90, billingCycle: "MONTHLY", nextBilling: "2024-02-10", status: "ACTIVE" },
  // ... mais exemplos
]

// Stats do dashboard
export const mockStats = {
  monthlySpending: 847.90,
  totalSubscriptions: 12,
  expiringThisWeek: 3,
  nextBillingDays: 5,
  nextBillingName: "Netflix"
}
```

---

## 6. Ordem de implementação (Fase 1)

1. Reorganizar estrutura de pastas (route groups)
2. Criar layout do app (Sidebar + AppHeader)
3. Criar dados mock
4. Criar StatCard e SubscriptionCard
5. Implementar página Dashboard
6. Implementar página Lista de Assinaturas

---

## 7. Próximas fases (futuro)

### Fase 2: CRUD de Assinaturas
- Formulário de criar/editar assinatura
- Modal de confirmação de exclusão
- Validação de campos

### Fase 3: Autenticação
- Página de login
- Página de registro
- Middleware de proteção de rotas
- Integração com backend auth

### Fase 4: Integração com API
- Conectar com backend (`localhost:6969`)
- Substituir mocks por chamadas reais
- Tratamento de erros e loading states

### Fase 5: Features adicionais
- Página de categorias
- Calendário de vencimentos
- Página de configurações
- Notificações de vencimento
- Gráficos de gastos

---

## Verificação

1. `npm run dev` - servidor inicia sem erros
2. Acessar `http://localhost:3000` - landing page funciona
3. Acessar `http://localhost:3000/dashboard` - dashboard renderiza com dados mock
4. Acessar `http://localhost:3000/subscriptions` - lista renderiza com dados mock
5. `npm run lint` - sem erros de lint
6. `npm run build` - build passa
