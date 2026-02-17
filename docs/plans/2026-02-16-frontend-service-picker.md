# Plano: Modal de Serviços Populares + Melhorias no Formulário de Assinaturas

## Contexto

A integração CRUD de subscriptions com o backend já está implementada, mas o fluxo de criação precisa de melhorias:

1. O formulário não tem seleção de **categoria** (o backend aceita `categoryId` mas o form não envia)
2. O campo `nextPaymentDate` é pedido ao usuário, mas o **backend calcula automaticamente** (
   `SubscriptionsService:31-32`)
3. A UX de criação é genérica - o usuário precisa preencher tudo manualmente, sem pré-preenchimento de serviços
   conhecidos

A solução: um modal em **dois passos** - primeiro o usuário escolhe um serviço popular (com logo, nome e dados
pré-preenchidos), depois preenche/ajusta o formulário.

## Arquivos

### Novos (2 arquivos)

| Arquivo                                                        | Descrição                                                                                                                                           |
|----------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `frontend/src/features/subscriptions/data/popular-services.ts` | Array de ~16 serviços populares (Netflix, Spotify, Disney+, etc.) com nome, logoUrl, categoryId, defaultBillingCycle, defaultValue, defaultCurrency |
| `frontend/src/components/app/ServicePicker.tsx`                | Componente Step 1: barra de busca + grid de cards de serviços populares + botão "Adicionar manualmente"                                             |

### Modificados (5 arquivos)

| Arquivo                                                           | Mudanças                                                                                                                                                      |
|-------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `frontend/src/features/subscriptions/types/subscription.types.ts` | Adicionar interfaces `PopularService` e `CategoryOption`                                                                                                      |
| `frontend/src/features/subscriptions/utils/subscription.utils.ts` | Adicionar `calculateNextPaymentDate()`, `CATEGORIES[]`, `categoryOptions[]`                                                                                   |
| `frontend/src/features/subscriptions/index.ts`                    | Re-exportar novos tipos, dados e utilitários                                                                                                                  |
| `frontend/src/components/app/SubscriptionForm.tsx`                | Remover `nextPaymentDate` do form, adicionar Select de categoria, aceitar prop `prefill` (PopularService) e `onBack`, auto-calcular nextPaymentDate no submit |
| `frontend/src/components/app/SubscriptionsPageContent.tsx`        | Orquestrar modal em 2 passos: Step 1 (ServicePicker) vs Step 2 (SubscriptionForm), gerenciar estado `modalStep` e `selectedService`                           |
| `frontend/src/components/app/index.ts`                            | Exportar `ServicePicker`                                                                                                                                      |

## Fluxo do Modal (2 passos)

```
Clique "Adicionar"
  → Modal abre no Step 1 (ServicePicker)
  → Barra de busca no topo para filtrar serviços populares
  → Se o serviço buscado existe na lista → clica no card → Step 2 com dados pré-preenchidos
  → Se o serviço NÃO existe na lista → aparece card especial "Criar [nome digitado]"
    → Clica nele → Step 2 com apenas o nome pré-preenchido
  → Botão "Voltar" no form retorna ao Step 1

Clique "Editar" numa assinatura existente
  → Modal abre direto no Step 2 (SubscriptionForm) com dados da assinatura
  → Sem botão "Voltar" (não faz sentido voltar ao picker)
```

## Detalhes de Implementação

### 1. `popular-services.ts` - Dados dos serviços

```typescript
interface PopularService {
  name: string;
  logoUrl: string;           // favicon/logo URL do serviço
  categoryId: number;        // ID da categoria no banco (1-9)
  defaultBillingCycle: BillingCycle;
  defaultValue: number;
  defaultCurrency: Currency;
}
```

~16 serviços: Netflix, Spotify, Disney+, YouTube Premium, Amazon Prime, Xbox Game Pass, iCloud, ChatGPT Plus, Adobe CC,
HBO Max, Duolingo, Notion, GitHub, Figma, Canva, Google One.

Logos: usar URLs de favicon dos serviços com fallback para iniciais (mesmo padrão do `SubscriptionCard` existente).
CategoryIds mapeiam para a seed `V1.1__insert_domains_value.sql`:

- 1=Video Streaming, 2=Music Streaming, 3=Gaming, 4=Software/SaaS, 5=Education, 6=Health, 7=Utilities, 8=Insurance,
  9=Other

### 2. `ServicePicker.tsx` - Componente de seleção

- Input de busca no topo (reutiliza `Input` existente com ícone `Search` do lucide-react)
- Heading "Serviços populares"
- Grid responsivo: `grid-cols-2 sm:grid-cols-3 lg:grid-cols-4`
- Cada card: logo (img com fallback para iniciais) + nome do serviço
- Hover: `border-primary-200 bg-primary-50/50` (consistente com design system)
- Grid scrollável: `max-h-[400px] overflow-y-auto`
- Busca filtra em tempo real via `useMemo`
- **Busca com criação**: quando o texto de busca não corresponde a nenhum serviço popular (ou corresponde parcialmente
  mas o usuário quer criar um novo), aparece um card especial com ícone "+" e texto "Criar [nome digitado]". Ao clicar,
  vai para Step 2 com apenas o `name` pré-preenchido (os demais campos ficam com defaults). O callback para este caso é
  `onCreateCustom(name: string)`.
- Props: `onSelectService(service: PopularService)`, `onCreateCustom(name: string)`

### 3. `SubscriptionForm.tsx` - Mudanças no formulário

**Remove:**

- Campo `nextPaymentDate` do `FormValues` e do JSX
- Validação de `nextPaymentDate`

**Adiciona:**

- Campo `categoryId` no `FormValues` (string para compatibilidade com Select)
- Select de Categoria usando `categoryOptions` hardcoded
- Props `prefill?: PopularService` e `onBack?: () => void`
- `getInitialValues` atualizado para aceitar `prefill` como segundo param
- No `handleSubmit`: calcula `nextPaymentDate` via `calculateNextPaymentDate(startDate, billingCycle)` antes de enviar
- Botão de voltar (arrow-left) quando `onBack` está definido
- Remove o `<h3>` título interno (o Modal já tem título no header)

**Layout do form:**

- Linha 1: Nome (full-width)
- Linha 2: Descrição (full-width)
- Linha 3: Valor + Moeda (2 cols)
- Linha 4: Data de início + Categoria (2 cols) ← nova combinação
- Linha 5: Ciclo de cobrança (full-width)
- Linha 6: Logo URL (full-width)
- Linha 7: Checkboxes ativo + notificar (2 cols)

### 4. `SubscriptionsPageContent.tsx` - Orquestração

**Novo estado:**

```typescript
type ModalStep = "picker" | "form";
const [modalStep, setModalStep] = useState<ModalStep>("picker");
const [selectedService, setSelectedService] = useState<PopularService | null>(null);
```

**Handlers atualizados:**

- `handleCreate` → abre modal no step "picker"
- `handleEdit` → abre modal direto no step "form" (pula picker)
- `handleServiceSelect(service)` → seta selectedService, muda para step "form"
- `handleCreateCustom(name)` → seta selectedService=null, guarda o nome customizado, muda para step "form" com só o nome
  pré-preenchido
- `handleBackToPicker` → volta para step "picker"
- `closeModal` → reseta tudo (isModalOpen, editingSubscription, selectedService, modalStep)

**Novo estado adicional:**

```typescript
const [customName, setCustomName] = useState<string | null>(null);
```

Usado quando o usuário digita um nome na busca e clica "Criar [nome]". O form recebe esse nome como pré-preenchimento
parcial (sem logo, sem categoria, sem valor default).

**Título do modal dinâmico:**

- Editando → "Editar assinatura"
- Step picker → "Adicionar assinatura"
- Step form com service popular → "Adicionar {nome do serviço}"
- Step form com nome customizado → "Adicionar {nome digitado}"
- Step form sem nada → "Nova assinatura"

### 5. `calculateNextPaymentDate` - Utilitário

Espelha a lógica do backend (`SubscriptionsService.calculateNextBillingDate`):

- MONTHLY → +1 mês
- QUARTERLY → +3 meses
- SEMI_ANNUAL → +6 meses
- YEARLY → +1 ano
- WEEKLY → +7 dias
- BIWEEKLY → +14 dias

Inclui clamping de fim de mês para edge cases (ex: 31 jan + 1 mês = 28/29 fev, não 2/3 mar). Retorna string
`YYYY-MM-DD`.

Nota: o backend recalcula de qualquer forma no `save()`, então este cálculo serve apenas para satisfazer o `@NotNull` do
DTO.

### 6. Categorias hardcoded

```typescript
export const CATEGORIES: CategoryOption[] = [
  { id: 1, name: "Video Streaming" },
  { id: 2, name: "Music Streaming" },
  { id: 3, name: "Gaming" },
  { id: 4, name: "Software / SaaS" },
  { id: 5, name: "Education" },
  { id: 6, name: "Health & Fitness" },
  { id: 7, name: "Utilities" },
  { id: 8, name: "Insurance" },
  { id: 9, name: "Other" },
];
```

Não existe endpoint `GET /categories` no backend - as categorias são dados fixos da migration.

## Ordem de Implementação

1. **Types + Data**: `subscription.types.ts` → `popular-services.ts` → `subscription.utils.ts` → `index.ts`
2. **ServicePicker**: `ServicePicker.tsx` → `components/app/index.ts`
3. **SubscriptionForm**: Modificar form (remover nextPaymentDate, adicionar categoria, prefill, back)
4. **SubscriptionsPageContent**: Wiring do modal em 2 passos
5. **Build verification**: `npm run build && npm run lint`

## Verificação

1. `cd frontend && npm run build` — sem erros de tipo
2. `cd frontend && npm run lint` — sem warnings
3. Teste manual:
    - Clicar "Adicionar" → modal abre com grid de serviços populares
    - Buscar "Netflix" → filtra os cards → clicar no card → form abre pré-preenchido
    - Buscar "Crunchyroll" (não está na lista) → aparece card "Criar Crunchyroll" → clica → form abre com nome
      pré-preenchido
    - Clicar "Voltar" no form → retorna ao picker
    - Preencher form + submit → assinatura criada (nextPaymentDate calculado automaticamente)
    - Editar assinatura existente → abre direto no form, sem picker, sem botão voltar
    - Dashboard (`/dashboard`) → continua funcionando com mock data
