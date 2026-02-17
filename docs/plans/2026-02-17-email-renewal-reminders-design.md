# Email Renewal Reminders — Design Document

**Data:** 2026-02-17
**Status:** Aprovado

## Resumo

Sistema de notificação por email que envia **1 email consolidado por usuário** com todas as assinaturas que renovam no
dia seguinte. Roda via `@Scheduled` uma vez por dia (08:00), usa Resend como provedor de email, e Redis para controle de
duplicatas.

## Decisões de Design

| Decisao                             | Escolha                          | Alternativas Consideradas         |
|-------------------------------------|----------------------------------|-----------------------------------|
| Provedor de email                   | Resend                           | Gmail SMTP, Amazon SES, Mailtrap  |
| Frequencia do scheduler             | 1x/dia (08:00)                   | 2x/dia, a cada hora               |
| Controle de duplicatas              | Redis (TTL 48h)                  | Tabela no banco, nenhum           |
| Email por assinatura vs consolidado | 1 email por usuario              | 1 email por assinatura            |
| Template engine                     | HTML inline (Java)               | Thymeleaf, FreeMarker             |
| Estilo visual                       | Seguir design system do frontend | Minimalista, dark mode, gradiente |

## Arquitetura

```
[Scheduler @8h] --> [NotificationScheduler]
                         |
                    [SubscriptionsService] busca e agrupa subs
                         |
                    [Repository] busca subs onde:
                         - nextPaymentDate = amanha
                         - active = true
                         - notify = true
                         |
                    Retorna Map<User, List<Subscriptions>>
                         |
                    Para cada usuario:
                         |
                    [Redis] verifica chave reminder:{userId}:{date}
                         |
                    Se nao existe:
                         |
                    [EmailTemplateBuilder] monta HTML
                         |
                    [EmailService] envia via Resend SDK
                         |
                    [Redis] seta chave com TTL 48h
```

## Novos Componentes

### Estrutura de arquivos

```
backend/src/main/java/dev/guilhermeluan/ongoing/
└── notification/
    ├── NotificationScheduler.java    # @Scheduled, orquestra o fluxo
    ├── EmailService.java             # Integracao com Resend SDK
    └── EmailTemplateBuilder.java     # Monta o HTML do email
```

### NotificationScheduler

O scheduler **nao acessa o repository diretamente** — segue o padrao do projeto (Scheduler -> Service -> Repository).

```java

@Component
@EnableScheduling
public class NotificationScheduler {

  private final SubscriptionsService subscriptionsService;
  private final EmailService emailService;
  private final StringRedisTemplate redis;

  @Scheduled(cron = "0 0 8 * * *")  // Todo dia as 08:00
    public void sendRenewalReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

    // 1. Busca assinaturas agrupadas por usuario (via Service)
    Map<User, List<Subscriptions>> grouped =
            subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow);

    // 2. Para cada usuario, verifica Redis e envia
        grouped.forEach((user, subs) -> {
            String redisKey = "reminder:" + user.getId() + ":" + tomorrow;
            if (!redis.hasKey(redisKey)) {
                emailService.sendRenewalReminder(user, subs, tomorrow);
                redis.opsForValue().set(redisKey, "sent", 48, TimeUnit.HOURS);
            }
        });
    }
}
```

### Novo metodo no SubscriptionsService

```java

public Map<User, List<Subscriptions>> findRenewalSubscriptionsGroupedByUser(LocalDate date) {
  List<Subscriptions> expiring = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(date);
  return expiring.stream().collect(Collectors.groupingBy(Subscriptions::getUser));
}
```

### Nova query no SubscriptionsRepository

```java

@Query("""
        SELECT s FROM Subscriptions s
        JOIN FETCH s.user
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.paymentMethod
        WHERE s.nextPaymentDate = :date
        AND s.active = true
        AND s.notify = true
        """)
List<Subscriptions> findByNextPaymentDateAndActiveAndNotify(@Param("date") LocalDate date);
```

### EmailService

Wrapper em torno do Resend SDK. Recebe `User`, `List<Subscriptions>`, e `LocalDate`, monta o email via
`EmailTemplateBuilder` e envia.

### EmailTemplateBuilder

Gera HTML inline seguindo o design system do frontend:

- **Cores**: primario verde (`#22c55e`), accent roxo (`#8b5cf6`), neutros
- **Gradiente no header**: `linear-gradient(135deg, #22c55e, #8b5cf6)`
- **Fonts**: Inter (body), Plus Jakarta Sans (headings) com fallback web-safe
- **Border-radius**: 12px nos cards
- **Sombras suaves**

**Layout do email:**

```
+----------------------------------------------+
|  Header gradiente verde -> roxo              |
|  Ongoing - Lembrete de Renovacao             |
+----------------------------------------------+
|                                              |
|  Ola, {userName}!                            |
|                                              |
|  Voce tem {count} assinatura(s) renovando    |
|  amanha ({date}):                            |
|                                              |
|  +----------------------------------------+  |
|  | Netflix        R$ 55,90    Mensal      |  |
|  | Cartao Nubank  - Entretenimento        |  |
|  +----------------------------------------+  |
|  | Spotify        R$ 21,90    Mensal      |  |
|  | Cartao Inter   - Entretenimento        |  |
|  +----------------------------------------+  |
|  | iCloud+        R$ 12,90    Mensal      |  |
|  | Cartao Nubank  - Tecnologia            |  |
|  +----------------------------------------+  |
|                                              |
|  Total: R$ 90,70                             |
|                                              |
|  [ Ver no Dashboard ]  (botao verde)         |
|                                              |
+----------------------------------------------+
|  Footer: Ongoing - Gerencie suas assinaturas |
+----------------------------------------------+
```

## Dependencias

### pom.xml

```xml

<dependency>
    <groupId>com.resend</groupId>
    <artifactId>resend-java</artifactId>
    <version>LATEST</version>
</dependency>
```

## Configuracao

### application.yaml

```yaml
resend:
  api-key: ${RESEND_API_KEY}
  from-email: ${RESEND_FROM_EMAIL:noreply@ongoing.com}

app:
  frontend-url: ${FRONTEND_URL:http://localhost:3000}
```

### Variaveis de ambiente (producao)

```env
RESEND_API_KEY=re_xxxxx
RESEND_FROM_EMAIL=noreply@seudominio.com
FRONTEND_URL=https://ongoing.up.railway.app
```

## Testes

### Unitarios

- **`NotificationSchedulerTest`**: Mocka service, redis e emailService
  - Verifica que chama service (nao repository diretamente)
    - Verifica que nao envia duplicata se chave Redis existe
    - Verifica que seta chave Redis apos envio

- **`EmailServiceTest`**: Mocka Resend SDK
    - Verifica que monta a request corretamente

- **`EmailTemplateBuilderTest`**:
    - Verifica que HTML gerado contem os dados corretos (nomes, valores, datas)

### Integracao

- Verificar query do repository com Testcontainers

## Observacoes

- O Resend exige dominio verificado para producao
- Em dev, usar o email de teste do Resend (`onboarding@resend.dev`) que entrega apenas para o email da conta
- O campo `notify` (boolean) na entidade `Subscriptions` ja existe e controla se o usuario quer receber lembrete para
  aquela assinatura especifica
- O `@EnableScheduling` deve ser colocado em uma classe `@Configuration` ou na classe principal da aplicacao
