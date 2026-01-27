# Design: Atualizar nextPaymentDate automaticamente via @Scheduled

**Data:** 2026-01-26
**Status:** Aprovado, aguardando implementacao

## Problema

Quando uma subscription e criada, o `nextPaymentDate` e calculado a partir do `startDate + 1 ciclo`. Porem, nao existe
nenhum mecanismo para **avancar** essa data quando ela vence. O campo fica congelado no passado.

## Decisao

Usar `@Scheduled` do Spring Boot para criar um cron job interno que roda **uma vez por dia a meia-noite** (
`0 0 0 * * *`).

### Por que @Scheduled e nao outras opcoes?

| Opcao                | Veredito   | Motivo                                                                                |
|----------------------|------------|---------------------------------------------------------------------------------------|
| **@Scheduled**       | Escolhida  | Zero infra extra, simples, ideal para instancia unica                                 |
| Spring Batch         | Descartada | Overhead excessivo (tabelas de controle, Reader/Processor/Writer) para o volume atual |
| Cron externo (SO/CI) | Descartada | Depende de infra externa + endpoint exposto precisa de autenticacao                   |

## Regra de negocio

- **Gatilho:** Job automatico (scheduled), sem intervencao manual
- **Calculo:** `nextPaymentDate = nextPaymentDate atual + 1 ciclo` (nao a partir de hoje)
    - billingCycleId 1 → +1 mes
    - billingCycleId 2 → +1 ano
- **Filtro:** Apenas subscriptions onde `nextPaymentDate <= hoje` AND `active = true`

## Arquitetura

```
@Scheduled (meia-noite)
       |
       v
SubscriptionScheduler (@Component)
       |
       v
SubscriptionsService.renewExpiredSubscriptions()
       |
       +-- Repository: findByNextPaymentDateLessThanEqualAndActiveTrue(today)
       |
       +-- Para cada subscription: nextPaymentDate += 1 ciclo
           +-- Repository.saveAll()
```

## Arquivos afetados

```
subscriptions/
+-- SubscriptionScheduler.java        <-- NOVO
+-- SubscriptionsService.java         <-- novo metodo renewExpiredSubscriptions()
+-- SubscriptionsRepository.java      <-- nova query
```

Alem disso, adicionar `@EnableScheduling` na classe principal ou em uma classe de configuracao.

### 1. SubscriptionsRepository

Nova query derivada:

```java
List<Subscriptions> findByNextPaymentDateLessThanEqualAndActiveTrue(LocalDate date);
```

### 2. SubscriptionsService

Novo metodo:

```java
public void renewExpiredSubscriptions() {
    List<Subscriptions> expired = repository
        .findByNextPaymentDateLessThanEqualAndActiveTrue(LocalDate.now());

    for (Subscriptions sub : expired) {
        LocalDate next = calculateNextBillingDate(sub); // ja existe, mas precisa usar nextPaymentDate como base em vez de startDate
        sub.setNextPaymentDate(next);
    }

    repository.saveAll(expired);
}
```

**Atencao:** O metodo `calculateNextBillingDate` atual usa `startDate` como base. Para o renewal, precisa ser ajustado
para usar `nextPaymentDate` como base do calculo.

### 3. SubscriptionScheduler (novo)

```java

@Component
public class SubscriptionScheduler {
    private final SubscriptionsService service;

    @Scheduled(cron = "0 0 0 * * *")
    public void renewExpiredSubscriptions() {
        service.renewExpiredSubscriptions();
    }
}
```

### 4. @EnableScheduling

Adicionar na classe principal `OngoingApplication` ou em uma `@Configuration` dedicada.

## O que NAO muda

- Nenhuma migration nova (o campo `next_payment_date` ja existe)
- Nenhum endpoint novo
- Nenhuma dependencia nova no `pom.xml`

## Riscos e observacoes

- **Instancia unica:** Se no futuro rodar multiplas instancias, o job executa em todas simultaneamente. Solucao:
  adicionar ShedLock.
- **Server desligado:** Se a aplicacao ficar fora do ar por varios dias, na proxima execucao o job processa tudo que
  ficou pendente (pois o filtro e `<= hoje`). Porem, avanca apenas 1 ciclo por execucao. Se ficou parado por 3 meses,
  precisaria de 3 execucoes (ou um loop no metodo).
