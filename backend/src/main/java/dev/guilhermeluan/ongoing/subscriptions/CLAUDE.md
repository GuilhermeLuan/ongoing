# Subscriptions Module

Módulo principal da aplicação - gerenciamento de assinaturas recorrentes do usuário.

## Componentes Principais

### SubscriptionsController

REST endpoints CRUD para assinaturas:

- `GET /api/v1/subscriptions` - Lista assinaturas com filtros e paginação
- `GET /api/v1/subscriptions/{id}` - Busca assinatura por ID
- `POST /api/v1/subscriptions` - Cria nova assinatura
- `PUT /api/v1/subscriptions/{id}` - Atualiza assinatura existente
- `DELETE /api/v1/subscriptions/{id}` - Remove assinatura

**Segurança**: Todos endpoints requerem autenticação JWT. O userId é extraído do `UserPrincipal` no SecurityContext.

### SubscriptionsService

Lógica de negócio:

- **findAll()** - Busca paginada com filtros opcionais (name, active, categoryId)
- **findByIdOrThrowNotFoundException()** - Busca por ID ou lança 404
- **save()** - Cria assinatura, calcula próxima data de cobrança
- **update()** - Atualiza assinatura existente
- **deleteById()** - Remove assinatura

**Pattern importante**: Todos métodos verificam que a assinatura pertence ao usuário autenticado (`userId`).

### SubscriptionsRepository

Interface Spring Data JPA com queries customizadas:

- **findByUserIdAndFilters()** - Query dinâmica com filtros opcionais
- **findByIdAndUserId()** - Busca por ID garantindo ownership

### SubscriptionsMapper

Interface MapStruct para conversão DTO ↔ Entity:

- **toSubscription()** - RequestDTO → Entity
- **toSubscriptionResponse()** - Entity → ResponseDTO
- **updateSubscriptionFromDto()** - Atualiza entity existente com dados do DTO

## Entities Package (entities/)

### Subscriptions

Entidade JPA principal:

```java
-id(Long, auto-increment)
-

name(String)
-

price(BigDecimal)
-

currency(Currency enum)
-

billingCycle(BillingCycle enum)
-

nextBillingDate(LocalDate) -
calculada automaticamente
-

category(Category enum)
-

paymentMethod(PaymentMethod enum)
-

subscriptionType(SubscriptionType enum)
-

active(Boolean)
-

userId(Long) -
foreign key
para User
-createdAt,

updatedAt(timestamps automáticos)
```

**Lifecycle Hooks**:

- `@PrePersist` - Define timestamps e calcula nextBillingDate
- `@PreUpdate` - Atualiza updatedAt

### Enums

Todos armazenados como STRING no banco:

- **BillingCycle**: MONTHLY, YEARLY, WEEKLY
- **Currency**: BRL, USD, EUR
- **Category**: STREAMING, PRODUCTIVITY, GAMING, HEALTH, EDUCATION, OTHER
- **PaymentMethod**: CREDIT_CARD, DEBIT_CARD, PIX, BOLETO, PAYPAL
- **SubscriptionType**: PERSONAL, FAMILY, BUSINESS

## DTOs Package (dto/)

### SubscriptionRequestDto

Record para criação/atualização:

- Todos campos com validações Bean Validation (@NotNull, @NotBlank, etc.)
- price com @Positive
- Sem nextBillingDate (calculado automaticamente)

### SubscriptionResponseDto

Record para respostas:

- Inclui todos campos da entidade
- Inclui nextBillingDate calculado
- Inclui timestamps

## Cálculo de Data de Cobrança

Implementado em `SubscriptionsService.calculateNextBillingDate()`:

- **MONTHLY**: Adiciona 1 mês à data atual
- **YEARLY**: Adiciona 1 ano à data atual
- **WEEKLY**: Adiciona 7 dias à data atual

Usa `LocalDate` do Java Time API.

## Filtros e Paginação

Endpoint `GET /api/v1/subscriptions` aceita:

- Query params: `name`, `active`, `categoryId`
- Paginação via Spring Data: `page`, `size`, `sort`
- Exemplo: `/api/v1/subscriptions?active=true&page=0&size=10&sort=name,asc`

## Multi-tenancy

Todas operações são **user-scoped**:

- Assinaturas sempre filtradas por `userId`
- Usuário só acessa suas próprias assinaturas
- Implementado em nível de serviço e repository

## Tratamento de Erros

- Assinatura não encontrada → `NotFoundException` (404)
- Validação falha → `MethodArgumentNotValidException` → 400
- Usuário tenta acessar assinatura de outro → `NotFoundException` (404)

## Testing

Testes de integração em `SubscriptionsControllerIT`:

- Testam CRUD completo com autenticação JWT
- Validam filtros e paginação
- Verificam isolamento entre usuários (multi-tenancy)
- Usam Testcontainers com PostgreSQL
