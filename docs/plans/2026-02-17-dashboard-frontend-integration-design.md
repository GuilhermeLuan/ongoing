# Plano: Integração do Dashboard com Backend (pós integração de Subscriptions)

## Contexto

Após a integração do módulo de subscriptions no frontend, o dashboard ainda usa `mock-data`.  
Objetivo desta etapa: conectar o dashboard com dados reais do backend, usando como contrato de referência:

- `DashboardController.java`
- `DashboardControllerIT.java`

Escopo validado:

- Conectar **cards** e **gastos por categoria** ao endpoint de dashboard.
- Manter **Próximos Vencimentos** vindo de subscriptions.
- Corrigir rota para `/api/v1/dashboard` **sem compatibilidade** com `/api/v1/dasboard`.
- Em erro de dashboard, mostrar erro localizado e manter página utilizável.
- Incluir testes unitários de service/hook de dashboard.

## Abordagens consideradas

1. **Server Component buscando backend direto**
    - Prós: menos JS no cliente.
    - Contras: mais acoplamento e menor reaproveitamento de padrão existente.

2. **Service + Hook no cliente (escolhida)**
    - Prós: consistente com `features/subscriptions`, melhor testabilidade, loading/erro padronizados.
    - Contras: um pouco mais de código no frontend.

3. **Híbrida com mock parcial**
    - Prós: menor esforço inicial.
    - Contras: duas fontes de verdade e maior risco de inconsistência.

## Design aprovado

### 1) Arquitetura e fluxo de dados

Criar módulo `features/dashboard` com:

- `types/dashboard.types.ts`
- `services/dashboard.service.ts`
- `hooks/useDashboard.ts`
- `index.ts` (barrel)

Contrato frontend deve espelhar `DashboardResponse`:

- `spendingByCategory`
- `monthlyAverage`
- `thisMonthTotal`
- `yearlyTotal`
- `currency`
- `exchangeRateDate`

`dashboard.service.ts` fará `GET /dashboard` via `apiClient`.

`dashboard/page.tsx` terá dois fluxos:

1. `useDashboard()` para cards + categoria;
2. `useSubscriptions()` para “Próximos Vencimentos”.

`lib/constants.ts` deve receber `API_ENDPOINTS.DASHBOARD = '/dashboard'`.

No backend:

- Atualizar `DashboardController` para `@RequestMapping("/api/v1/dashboard")`.
- Atualizar `DashboardControllerIT` para nova rota.

### 2) Erros, estados e testes

Estados previstos:

- **Loading**: skeleton/placeholder local para cards e categorias.
- **Erro**: bloco de erro apenas na área do dashboard, com ação de retry (`fetchDashboard()`).
- **Vazio**: resposta zerada não é erro; mostrar estado sem dados.

Comportamento preservado:

- Header, navegação e lista de próximos vencimentos continuam funcionais mesmo com falha no dashboard.

Testes desta entrega:

- Unitário de `dashboard.service` (endpoint e parse).
- Unitário de `useDashboard` (loading, sucesso, erro e retry).
- Teste leve de mapeamento/formatação (BRL e percentual com divisão por zero segura).
- Backend: manter robustez dos cenários do `DashboardControllerIT`, apenas ajustando rota.

## Critérios de aceite

- Dashboard sem mock para cards e categorias.
- Próximos vencimentos continuam via subscriptions.
- Rota de dashboard padronizada para `/api/v1/dashboard`.
- Erro localizado com retry.
- Testes unitários novos passando.
