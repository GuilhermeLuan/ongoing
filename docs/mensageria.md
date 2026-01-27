# Mensageria no Ongoing

## Por que esse documento existe?

O projeto hoje e um CRUD sincronico puro. Nenhuma operacao acontece fora do ciclo request-response do Spring MVC. Esse
documento explora **onde message brokers fariam sentido** se o projeto evoluisse, e inclui um estudo de caso pratico:
disparar e-mails de lembrete quando uma assinatura esta prestes a renovar.

---

## O que e um Message Broker?

Pensa num broker como os Correios. Voce (producer) coloca uma carta (mensagem) na caixa e segue sua vida. Os Correios (
broker) cuidam de entregar a carta ao destinatario (consumer). Voce nao precisa saber onde o destinatario mora, nao
precisa esperar ele estar em casa, e se ele nao estiver, os Correios tentam de novo depois.

No mundo de software, brokers como **RabbitMQ** e **Apache Kafka** fazem exatamente isso entre servicos ou modulos de um
sistema.

Conceitos-chave:

| Termo              | O que e                                      | Analogia              |
|--------------------|----------------------------------------------|-----------------------|
| **Producer**       | Quem envia a mensagem                        | Voce postando a carta |
| **Consumer**       | Quem recebe e processa                       | O destinatario        |
| **Queue/Topic**    | Canal onde as mensagens ficam                | A caixa postal        |
| **Acknowledgment** | Confirmacao de que a mensagem foi processada | Aviso de recebimento  |

---

## Onde mensageria se aplicaria nesse projeto?

### 1. Notificacoes de cobranca (e-mail de renovacao)

A entidade `Subscriptions` ja tem os campos `notify` e `nextPaymentDate`, mas nenhum processamento assincrono existe. Um
fluxo natural seria:

- Um **scheduled job** detecta assinaturas com `nextPaymentDate` proximo
- Publica um evento num topico (ex: `subscription.payment.due`)
- **Consumers independentes** processam isso: um envia e-mail, outro push notification, outro SMS

O broker desacopla quem detecta a cobranca de quem notifica o usuario. Sem ele, o job precisaria conhecer todos os
canais de notificacao.

### 2. Integracao com gateway de pagamento

Se o sistema processasse pagamentos de fato:

- O servico publica `payment.requested` no broker
- Um consumer integra com o gateway (Stripe, Mercado Pago, etc.)
- O gateway responde via webhook, que publica `payment.confirmed` ou `payment.failed`
- Consumers atualizam o status da assinatura, recalculam `nextPaymentDate`, enviam recibo

Chamadas a APIs externas falham. O broker da **retry automatico** e **garantia de entrega** — se o gateway estiver fora
do ar, a mensagem fica na fila e e reprocessada depois.

### 3. Eventos de dominio (Event-Driven)

Acoes no CRUD poderiam gerar eventos:

| Acao                | Evento                       | Consumers possiveis               |
|---------------------|------------------------------|-----------------------------------|
| Criar assinatura    | `subscription.created`       | Analytics, e-mail de boas-vindas  |
| Cancelar assinatura | `subscription.cancelled`     | Pesquisa de churn, revogar acesso |
| Atualizar valor     | `subscription.price.changed` | Notificacao ao usuario            |

Isso transforma o monolito CRUD num sistema reativo onde novos comportamentos sao adicionados criando consumers, sem
tocar no codigo existente.

### 4. Atualizacao de cambio

O projeto suporta `BRL`, `USD`, `EUR`. Se precisasse atualizar cotacoes:

- Um producer publica cotacoes periodicamente
- Consumers recalculam o `value` das assinaturas em moeda estrangeira

---

## Estudo de caso: e-mail de renovacao de assinatura

### O cenario

O usuario tem uma assinatura da Netflix que renova dia 15. Queremos enviar um e-mail 3 dias antes avisando: "Sua
assinatura da Netflix de R$ 55,90 renova em 3 dias."

### Sem mensageria (abordagem simples)

```
@Scheduled -> busca assinaturas proximas -> envia e-mail direto
```

```java
@Scheduled(cron = "0 0 8 * * *") // todo dia as 8h
public void notifyUpcomingRenewals() {
    LocalDate threshold = LocalDate.now().plusDays(3);
    List<Subscriptions> upcoming = repository
        .findByNextPaymentDateAndNotifyTrue(threshold);

    for (Subscriptions sub : upcoming) {
        emailService.send(sub); // sincrono, bloqueia ate enviar
    }
}
```

**Problemas dessa abordagem:**

- Se o servidor de e-mail estiver fora do ar, o usuario nao recebe nada
- Se tiver 10.000 assinaturas para notificar, o loop leva tempo e nao tem paralelismo
- Se voce quiser adicionar SMS, precisa mexer nesse mesmo metodo
- Se o envio de um e-mail falhar, os proximos podem nao ser processados
- Nao tem retry: falhou, perdeu

### Com mensageria (abordagem desacoplada)

```
@Scheduled -> busca assinaturas -> publica eventos na fila
                                          |
                              +-----------+-----------+
                              |                       |
                        EmailConsumer            SMSConsumer
                     (processa no ritmo dele)  (independente)
```

```java
// === PRODUCER: o scheduled job so publica eventos ===

@Scheduled(cron = "0 0 8 * * *")
public void notifyUpcomingRenewals() {
    LocalDate threshold = LocalDate.now().plusDays(3);
    List<Subscriptions> upcoming = repository
        .findByNextPaymentDateAndNotifyTrue(threshold);

    for (Subscriptions sub : upcoming) {
        // Nao envia e-mail aqui. So publica o evento.
        rabbitTemplate.convertAndSend(
            "subscription.exchange",
            "subscription.renewal.upcoming",
            new RenewalEvent(sub.getId(), sub.getName(),
                             sub.getValue(), sub.getNextPaymentDate())
        );
    }
}
```

```java
// === CONSUMER: processa cada evento de forma independente ===

@RabbitListener(queues = "renewal-email-queue")
public void handleRenewalNotification(RenewalEvent event) {
    emailService.sendRenewalReminder(
        event.subscriptionName(),
        event.value(),
        event.nextPaymentDate()
    );
}
```

```java
// === O EVENTO: um record simples ===

public record RenewalEvent(
    Long subscriptionId,
    String subscriptionName,
    BigDecimal value,
    LocalDate nextPaymentDate
) {}
```

### O que a mensageria resolve aqui

| Problema                | Sem broker            | Com broker                                   |
|-------------------------|-----------------------|----------------------------------------------|
| Servidor de e-mail fora | E-mail perdido        | Mensagem fica na fila, reenviada depois      |
| 10k notificacoes        | Loop sequencial       | Consumers paralelos processam a fila         |
| Adicionar SMS           | Mexe no scheduled job | Cria novo consumer, zero mudanca no producer |
| Um envio falha          | Pode travar o loop    | So aquela mensagem volta pra fila (retry)    |
| Rastreabilidade         | Log manual            | Broker rastreia cada mensagem                |

### Infraestrutura necessaria (com RabbitMQ)

No `docker-compose.yaml`, bastaria adicionar:

```yaml
rabbitmq:
  image: rabbitmq:4-management
  ports:
    - "5672:5672"   # AMQP (protocolo de mensagem)
    - "15672:15672" # Painel de gerenciamento (UI web)
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

No `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

Configuracao no `application.yaml`:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

---

## RabbitMQ vs Kafka: qual usar?

Para esse projeto, **RabbitMQ** e a escolha certa.

| Criterio           | RabbitMQ                                      | Kafka                                                          |
|--------------------|-----------------------------------------------|----------------------------------------------------------------|
| Modelo             | Fila de mensagens (message queue)             | Log distribuido (event stream)                                 |
| Quando usar        | Tarefas assincronas, notificacoes, integracao | Streaming de dados, alta vazao, event sourcing                 |
| Complexidade       | Simples de operar                             | Precisa de Zookeeper/KRaft, mais config                        |
| Mensagem consumida | Removida da fila apos ack                     | Permanece no topico por tempo configurado                      |
| Escala             | Milhares/seg                                  | Milhoes/seg                                                    |
| Analogia           | Correios: entregou, acabou                    | Netflix: o episodio fica la, qualquer um assiste quando quiser |

**RabbitMQ** para esse projeto porque:

- O volume e baixo (centenas de assinaturas, nao milhoes)
- Queremos "dispara e esquece" (nao precisamos reler eventos antigos)
- A configuracao e simples e o painel web ajuda no aprendizado
- Spring Boot tem suporte nativo via `spring-boot-starter-amqp`

---

## Quando mensageria e overengineering?

Sinais de que voce **nao** precisa de um broker:

- O sistema tem um unico consumer (so e-mail, sem SMS, sem push)
- O volume e baixo e voce aceita perder uma notificacao eventual
- Nao tem integracao com servicos externos que falham
- A equipe e pequena e a complexidade operacional do broker nao se justifica

Nesse caso, alternativas mais leves:

1. **`@Async` + `@EventListener`** do Spring: desacopla dentro do mesmo processo, sem infra extra
2. **`@Scheduled` direto**: para jobs simples onde falha eventual e aceitavel
3. **Outbox pattern com polling**: grava eventos numa tabela do banco e um job processa — sem broker externo

### O meio-termo: Spring Events

```java
// Publica evento dentro do proprio Spring (sem broker externo)
applicationEventPublisher.publishEvent(new RenewalEvent(sub));

// Consumer no mesmo processo
@EventListener
@Async
public void onRenewal(RenewalEvent event) {
    emailService.send(event);
}
```

Isso da desacoplamento sem infraestrutura extra. A desvantagem e que se a aplicacao cair, os eventos em andamento se
perdem (nao tem fila persistente).

---

## Resumo: arvore de decisao

```
Precisa notificar o usuario?
├── Sim
│   ├── So e-mail, volume baixo, falha aceitavel?
│   │   └── @Scheduled + envio direto (KISS)
│   ├── Quer desacoplar mas sem infra extra?
│   │   └── Spring Events + @Async
│   ├── Multiplos canais (e-mail + SMS + push)?
│   │   └── RabbitMQ
│   └── Milhoes de eventos, event sourcing, replay?
│       └── Kafka
└── Nao
    └── CRUD sincronico ta otimo, segue assim
```

Para esse projeto como estudo, **RabbitMQ** e o sweet spot: complexo o suficiente para aprender os conceitos de
mensageria, simples o suficiente para nao virar um projeto de infraestrutura.
