# Plano de Refactor: SubscriptionsControllerIT Test Utils

**Data:** 2026-02-17  
**Status:** Planejado  
**Escopo:** `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsControllerIT.java`

---

## Contexto

O `SubscriptionsControllerIT` concentra lógica de fixture com métodos repetidos e sobrecarregados, o que reduz
legibilidade e dificulta manutenção do teste.

Hoje existem no próprio IT:

- `insertSampleSubscriptions()`
- `createSubscription(...)` com 4 overloads

Esses métodos mudam pouco entre si e diferem principalmente por parâmetros.

---

## Objetivo

Extrair **somente** os helpers de criação de entidade para uma classe utilitária dedicada (`SubscriptionsTestUtils`),
mantendo assinaturas atuais para reduzir risco e evitar mudanças funcionais nos testes.

---

## Estado atual (diagnóstico)

- Não há `TestUtils` para subscriptions em `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/`.
- Parte dos helpers depende de estado implícito do teste (`authenticatedUser` e `subscriptionsRepository`).
- Há duplicação de estrutura de builder (`Subscriptions.builder()`) nos overloads.
- Helpers de request (`createSubscriptionRequestDTO` e `provideInvalidSubscriptionRequests`) ficam fora deste escopo.

---

## Estratégia proposta

Criar `SubscriptionsTestUtils` como classe `final` com construtor privado e métodos `public static`, preservando a API
atual dos helpers.

Princípios:

1. Refactor estrutural, sem alteração de comportamento.
2. Defaults preservados (`Currency.BRL`, `notify=true`, descrição `name + " mensal"` etc.).
3. Dependências implícitas passam a ser explícitas via parâmetros.
4. Mudanças mínimas no `SubscriptionsControllerIT`.

---

## Workplan

- [ ] Criar `SubscriptionsTestUtils` em `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/`.
- [ ] Mover para a util:
    - [ ] `insertSampleSubscriptions(...)`
    - [ ] `createSubscription(String, BigDecimal, LocalDate, LocalDate, BillingCycle, User, Category, PaymentMethod)`
    - [ ] `createSubscription(String, BigDecimal, LocalDate, LocalDate, BillingCycle, User)`
    - [ ] `createSubscription(String, BigDecimal, boolean, Category)`
    - [ ] `createSubscription(String, BigDecimal, boolean, Category, PaymentMethod)`
- [ ] Parametrizar dependências explícitas onde necessário (`SubscriptionsRepository`, `User`).
- [ ] Atualizar chamadas no `SubscriptionsControllerIT` para `SubscriptionsTestUtils.*`.
- [ ] Remover métodos privados duplicados do `SubscriptionsControllerIT`.
- [ ] Revisar imports e manter organização/estilo do projeto.

---

## Validação

- [ ] Rodar teste alvo: `cd backend && ./mvnw test -Dtest=SubscriptionsControllerIT`
- [ ] Rodar regressão de unidade: `cd backend && ./mvnw test`

Critério de sucesso:

- Nenhuma mudança funcional nos asserts e cenários existentes.
- Redução de duplicação e melhoria de legibilidade no IT.

---

## Notas

- Nome confirmado da util: `SubscriptionsTestUtils`.
- Decisão confirmada: manter assinaturas sobrecarregadas atuais (mudança mínima).
- Fora de escopo: extração de helpers de request/validação e refactor de outros módulos (ex.: dashboard).
