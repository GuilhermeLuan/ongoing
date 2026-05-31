# Plano de Implementação: Histórico de Reajuste + Detecção de Price Spike

## Resumo
Implementar no backend o registro automático de mudança de preço de assinatura e a marcação de aumentos relevantes (`price spike`) quando o valor subir `>= 10%`. O plano abaixo é incremental para execução e validação passo a passo.

## Passo 1: Migration da nova tabela
1. Criar `V6__create_subscription_price_history.sql`.
2. Criar tabela `tb_subscription_price_history` com:
- `id` (PK)
- `subscription_id` (FK para `tb_subscriptions.id`)
- `user_id` (FK para `users.id` ou tabela de usuário atual do projeto)
- `old_value` numeric(10,2) not null
- `new_value` numeric(10,2) not null
- `change_percentage` numeric(8,2) not null
- `is_price_spike` boolean not null default false
- `changed_at` timestamp not null default now()
3. Criar índice para consulta:
- `(subscription_id, changed_at desc)`
- `(user_id, is_price_spike, changed_at desc)`

Critério de conclusão: migration sobe sem erro.

## Passo 2: Modelagem JPA + Repository
1. Criar entidade `SubscriptionPriceHistory` no domínio de `subscriptions`.
2. Relacionamentos:
- `ManyToOne` para `Subscriptions`
- `ManyToOne` para `User`
3. Criar `SubscriptionPriceHistoryRepository` com métodos:
- listar histórico por assinatura+usuário ordenado por data desc
- listar spikes por usuário em intervalo de datas

Critério de conclusão: aplicação compila com a nova entidade e repository.

## Passo 3: Regra de negócio no update de assinatura
1. No `SubscriptionsService.update(...)`, buscar estado atual da assinatura antes do save.
2. Comparar `existing.value` com `incoming.value`.
3. Se mudou (`compareTo != 0`):
- calcular `%`: `((new - old) / old) * 100`
- definir spike quando `% >= 10`
- persistir linha em `tb_subscription_price_history`
4. Se não mudou, não registrar histórico.
5. Manter comportamento atual de `nextPaymentDate` e update da assinatura.

Regra de borda obrigatória:
- se `old_value == 0`, definir `change_percentage = 0` e `is_price_spike = false` para evitar divisão por zero.

Critério de conclusão: update continua funcionando e histórico é criado apenas quando há alteração real de valor.

## Passo 4: Endpoints de leitura
1. Adicionar `GET /api/v1/subscriptions/{id}/price-history`.
- validar ownership via `userId` autenticado
- retorno ordenado por `changedAt` desc
2. Adicionar `GET /api/v1/subscriptions/price-spikes?from=YYYY-MM-DD&to=YYYY-MM-DD`.
- default sugerido: `from=today-30d`, `to=today` quando não vier query param
- retorna apenas registros com `is_price_spike=true` do usuário autenticado

Critério de conclusão: endpoints retornam dados corretos e sem vazamento entre usuários.

## Passo 5: DTOs e mapeamento
1. Criar DTO de resposta para histórico (id, subscriptionId, oldValue, newValue, changePercentage, isPriceSpike, changedAt).
2. Criar mapper manual ou MapStruct para converter entidade -> DTO.
3. Padronizar serialização de data/hora conforme padrão atual da API.

Critério de conclusão: payloads estáveis e consistentes com o restante do backend.

## Passo 6: Testes
1. Unit/Service tests:
- cria histórico quando preço muda
- não cria quando não muda
- marca spike com aumento >=10%
- não quebra em `old_value=0`
2. Integration tests (controller):
- histórico por assinatura do próprio usuário
- bloqueio para assinatura de outro usuário
- filtro de intervalo no endpoint de spikes

Critério de conclusão: testes novos passando com `./mvnw test` (e `./mvnw verify` se incluir IT).

## Passo 7: Validação final manual
1. Criar assinatura de teste.
2. Atualizar valor sem mudança e confirmar: sem novo histórico.
3. Atualizar com aumento pequeno (<10%) e confirmar: histórico com `is_price_spike=false`.
4. Atualizar com aumento >=10% e confirmar: histórico com `is_price_spike=true`.
5. Consultar os 2 endpoints e validar ordenação/filtros.

## Assumptions (defaults escolhidos)
- Spike definido como aumento percentual `>= 10%`.
- Histórico registra apenas mudanças de `value`.
- `changed_at` em timestamp do servidor.
- Sem notificação automática nesta primeira versão (somente persistência e consulta).
