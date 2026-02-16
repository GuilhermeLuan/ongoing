# Plano: Integração Frontend com Endpoints de Subscriptions

## Contexto

A integração JWT já está completa (`features/auth/`), com `apiClient` (axios + interceptors) pronto para uso. Agora
precisamos conectar o frontend aos endpoints CRUD de subscriptions do backend, substituindo os mock data por chamadas
reais à API.

**Escopo:** CRUD completo (listar, criar, editar, deletar) com modal para formulários. Dashboard continua com mock data.

## Arquivos Novos (8 arquivos)

### 1. `frontend/src/features/subscriptions/types/subscription.types.ts`

- Interfaces TypeScript que espelham os DTOs do backend
- `SubscriptionResponse` (campos: id, name, description, value, startDate, nextPaymentDate, active, notifyUser,
  currency, logoUrl, categoryId, paymentMethodId, billingCycle, subscriptionTypeId)
- `SubscriptionRequest` (mesmos campos sem id)
- `Page<T>` genérico para paginação Spring Data (content, totalElements, totalPages, number, etc.)
- `SubscriptionFilters` (name?, active?, categoryId?, page?, size?)
- Enums: `BillingCycle` (6 valores: MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY, WEEKLY, BIWEEKLY), `Currency` (BRL, USD,
  EUR)

### 2. `frontend/src/features/subscriptions/services/subscription.service.ts`

- Usa o `apiClient` existente de `features/auth`
- Usa `API_ENDPOINTS.SUBSCRIPTIONS` de `lib/constants.ts`
- Funções: `findAll(filters)`, `findById(id)`, `create(data)`, `update(id, data)`, `remove(id)`

### 3. `frontend/src/features/subscriptions/hooks/useSubscriptions.ts`

- Hook customizado com estado (subscriptions, page, isLoading, error)
- Operações: `fetchSubscriptions(filters)`, `createSubscription(data)`, `updateSubscription(id, data)`,
  `deleteSubscription(id)`
- Auto-fetch no mount com filtros iniciais opcionais
- Mutations não fazem auto-refetch — o componente pai controla quando refrescar

### 4. `frontend/src/features/subscriptions/utils/subscription.utils.ts`

- Label maps em pt-BR: `billingCycleLabels` (6 valores), `currencyLabels`
- `formatSubscriptionValue(value, currency)` — formatação com locale correto
- `formatDate(dateString)`, `getDaysUntilBilling(dateString)`

### 5. `frontend/src/features/subscriptions/index.ts`

- Barrel exports seguindo o padrão de `features/auth/index.ts`

### 6. `frontend/src/components/ui/Modal.tsx`

- Componente UI reutilizável com `createPortal` (z-50, acima do sidebar z-30)
- Props: isOpen, onClose, title, children, size (sm/md/lg)
- Fecha com Escape e click no backdrop
- Trava scroll do body quando aberto
- Usa animações existentes do Tailwind config (fadeIn, fadeInUp)

### 7. `frontend/src/components/app/SubscriptionForm.tsx`

- Formulário para criar/editar dentro do modal
- Props: `subscription?` (SubscriptionResponse para modo edição), `onSubmit`, `onCancel`, `isSubmitting`
- Campos: nome, descrição, valor + moeda, data início + próximo pagamento, ciclo de cobrança, ativo, notificar, logo URL
- Validação client-side espelhando as constraints do backend (name required max 255, value positive, dates required,
  billingCycle required)
- Converte FormData (strings de inputs) para SubscriptionRequest (tipos corretos) antes de submeter

### 8. `frontend/src/components/app/SubscriptionsPageContent.tsx`

- Componente "use client" que orquestra toda a página
- Usa `useSubscriptions` hook para dados e operações
- Gerencia estados: modal aberto/fechado, modo criar vs editar, filtros, paginação
- Renderiza: header com botão "Adicionar", error banner, SubscriptionList, Modal com SubscriptionForm
- Delete usa `window.confirm` para confirmação simples
- Após mutações (create/update/delete): refetch com filtros atuais

## Arquivos Modificados (5 arquivos)

### 9. `frontend/src/components/app/SubscriptionCard.tsx`

- Prop type muda de `Subscription` (mock) para `SubscriptionResponse` (backend)
- Imports de `@/features/subscriptions` em vez de `@/lib/mock-data`
- Mapeamento de campos: `value` em vez de `price`, `nextPaymentDate` em vez de `nextBilling`, `active` (boolean) em vez
  de `status` (string)
- Badge "Inativa" quando `!subscription.active` (substituindo "Pausada" quando status === "PAUSED")
- Mostra `subscription.description` em vez de `categoryLabels[category]` (categoryId é numérico, sem lookup de nome por
  enquanto)
- Cor do avatar: gera deterministicamente a partir do nome (hash simples) já que `color` não existe no backend
- Adiciona props `onEdit?` e `onDelete?` para ações

### 10. `frontend/src/components/app/SubscriptionList.tsx`

- Props novas: `currentPage`, `totalPages`, `onPageChange`, `onFilterChange(filters)`, `onEdit(sub)`, `onDelete(sub)`
- Filtros delegam para o pai via callbacks (backend filtra, não mais client-side)
- Search input → `onFilterChange({ name })`, Status select → `onFilterChange({ active })`
- Remove filtro de categoria (backend usa categoryId numérico, sem endpoint de categorias ainda)
- Adiciona controles de paginação no rodapé (Anterior/Próximo + "Página X de Y")

### 11. `frontend/src/app/(app)/subscriptions/page.tsx`

- Simplifica para server component shell + `SubscriptionsPageContent` client component
- Mantém `export const metadata` para SEO
- Remove imports de mock data

### 12. `frontend/src/components/ui/index.ts`

- Adiciona export do `Modal`

### 13. `frontend/src/components/app/index.ts`

- Adiciona exports: `SubscriptionForm`, `SubscriptionsPageContent`

## Compatibilidade do Dashboard

O dashboard usa `SubscriptionCard` com a variante `compact` e dados mock (tipo `Subscription` antigo). Como vamos mudar
o tipo do SubscriptionCard para `SubscriptionResponse`, precisamos de uma adaptação mínima:

- Adicionar em `lib/mock-data.ts` uma função `toSubscriptionResponse()` que converte o mock `Subscription` →
  `SubscriptionResponse`
- No `dashboard/page.tsx`: trocar `mockSubscriptions` por `mockSubscriptions.map(toSubscriptionResponse)` (~1 linha de
  mudança)

Isso é necessário para evitar duplicação de componentes e manter type safety. É uma mudança puramente técnica, não
funcional.

## Ordem de Implementação

```
1. types/subscription.types.ts          (sem dependências)
2. services/subscription.service.ts     (depende de 1 + apiClient existente)
   utils/subscription.utils.ts          (depende de 1, paralelo com service)
3. hooks/useSubscriptions.ts            (depende de 1, 2)
4. index.ts barrel                      (depende de 1-3)
5. Modal.tsx                            (sem dependência do feature)
6. SubscriptionForm.tsx                 (depende de 1, 5)
7. SubscriptionCard.tsx (update)        (depende de 1, utils)
   SubscriptionList.tsx (update)        (depende de 1, 7-card)
   mock-data.ts (adapter function)      (depende de 1)
   dashboard/page.tsx (1-line fix)      (depende de 7-card, mock-data)
8. SubscriptionsPageContent.tsx         (depende de 3, 5, 6, 7)
   subscriptions/page.tsx (update)      (depende de 8)
9. Barrel exports + build verification
```

## Verificação

1. `cd frontend && npm run build` — sem erros de tipo
2. `cd frontend && npm run lint` — sem warnings
3. Teste manual:
    - Login → navegar para /subscriptions → deve carregar lista vazia da API
    - Clicar "Adicionar" → preencher form → submit → subscription aparece na lista
    - Clicar editar → alterar dados → submit → dados atualizados
    - Clicar deletar → confirmar → subscription removida
    - Testar filtros: busca por nome, filtro ativo/inativo
    - Testar paginação (se houver > 12 items)
    - Dashboard (/dashboard) → deve continuar funcionando com mock data
4. `cd backend && ./mvnw verify` — garantir que backend não foi alterado
