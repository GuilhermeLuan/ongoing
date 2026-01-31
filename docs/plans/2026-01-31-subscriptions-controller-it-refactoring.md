# Plano de Refatoração: SubscriptionsControllerIT

**Data:** 2026-01-31
**Status:** Aprovado
**Referência:** [JWT Authentication Design](./2026-01-29-jwt-authentication-design.md)

---

## Contexto

Com a implementação de autenticação JWT, os testes de integração do `SubscriptionsControllerIT` precisam ser adaptados
para:

1. Garantir que todas as requests incluam token de autenticação
2. Testar cenários de acesso não autorizado (401)
3. Testar isolamento entre usuários (User A não vê dados de User B)
4. Corrigir helpers que não associam subscriptions ao usuário

---

## Problemas Identificados

### 1. Dois `@BeforeEach` separados

```java
@BeforeEach
void cleanDatabase() { ... }

@BeforeEach
void setUpAuth() { ... }
```

**Problema:** A ordem de execução não é garantida pelo JUnit.

### 2. Métodos `createSubscription` sem User

```java
private Subscriptions createSubscription(String name, BigDecimal value, boolean active, Category category) {
    // ❌ Não seta user — viola constraint NOT NULL
}
```

**Afeta:** Testes de filtro nas linhas 367-462.

### 3. Testes de isolamento entre usuários — AUSENTES

O design especifica:
> "Adicionar teste: usuário A não vê subscriptions do usuário B"

### 4. Testes de autenticação (401) — AUSENTES

O design especifica:

- GET /subscriptions sem token → 401
- GET /subscriptions com token expirado → 401
- GET /subscriptions com token válido → 200

---

## Estrutura Proposta

### Setup Unificado

```java
@Autowired
private SubscriptionsRepository subscriptionsRepository;
@Autowired
private UserRepository userRepository;
@Autowired
private RefreshTokenRepository refreshTokenRepository;
@Autowired
private AuthService authService;

private String authToken;
private User authenticatedUser;

@BeforeEach
void setUp() {
    // 1. Limpa na ordem correta (dependências primeiro)
    subscriptionsRepository.deleteAll();
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();

    // 2. Cria usuário e obtém token
    RegisterRequest request = new RegisterRequest(
            "Test User",
            "test@example.com",
            "password123"
    );
    AuthResponse response = authService.register(request);

    this.authToken = response.accessToken();
    this.authenticatedUser = userRepository.findByEmail(request.email()).orElseThrow();
}
```

### Helper Simplificado

```java
private Subscriptions createSubscription(String name, BigDecimal value, boolean active, Category category) {
    return Subscriptions.builder()
            .name(name)
            .description(name + " mensal")
            .value(value)
            .startDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .billingCycle(BillingCycle.builder().id(1L).build())
            .currency(Currency.BRL)
            .notify(true)
            .active(active)
            .category(category)
            .user(authenticatedUser)  // ✅ Sempre associa ao usuário autenticado
            .build();
}
```

---

## Novos Testes

### Testes de Autenticação (401)

```java
@Test
void findAll_ShouldReturn401_WhenNoTokenProvided() {
    given()
        .contentType(ContentType.JSON)
        .when().get(API_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
}

@Test
void findById_ShouldReturn401_WhenNoTokenProvided() {
    given()
        .contentType(ContentType.JSON)
        .when().get(API_URL + "/{id}", 1)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
}

@Test
void create_ShouldReturn401_WhenNoTokenProvided() {
    given()
        .contentType(ContentType.JSON)
        .body(createSubscriptionRequestDTO())
        .when().post(API_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
}

@Test
void update_ShouldReturn401_WhenNoTokenProvided() {
    given()
        .contentType(ContentType.JSON)
        .body(createSubscriptionRequestDTO())
        .when().put(API_URL + "/{id}", 1)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
}

@Test
void delete_ShouldReturn401_WhenNoTokenProvided() {
    given()
        .contentType(ContentType.JSON)
        .when().delete(API_URL + "/{id}", 1)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
}
```

### Testes de Isolamento entre Usuários

```java
@Test
void findAll_ShouldNotReturnSubscriptionsFromOtherUsers() {
    // 1. Cria subscription do usuário autenticado
    Subscriptions mySubscription = subscriptionsRepository.save(
        createSubscription("Netflix", new BigDecimal("39.95"), true, null)
    );

    // 2. Cria outro usuário
    AuthResponse otherUserAuth = authService.register(
        new RegisterRequest("Other User", "other@example.com", "password123")
    );
    User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

    // 3. Cria subscription do outro usuário
    Subscriptions otherSubscription = subscriptionsRepository.save(
        Subscriptions.builder()
            .name("Spotify")
            .description("Spotify mensal")
            .value(new BigDecimal("19.95"))
            .startDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .billingCycle(BillingCycle.builder().id(1L).build())
            .currency(Currency.BRL)
            .active(true)
            .notify(true)
            .user(otherUser)
            .build()
    );

    // 4. Busca com token do primeiro usuário
    String response = given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + authToken)
        .when().get(API_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().body().asString();

    // 5. Deve ver apenas SUA subscription
    assertThatJson(response).node("totalElements").isEqualTo(1);
    assertThatJson(response).node("content[0].name").isEqualTo("Netflix");
}

@Test
void findById_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
    // 1. Cria outro usuário com subscription
    authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
    User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

    Subscriptions otherSubscription = subscriptionsRepository.save(
        Subscriptions.builder()
            .name("Spotify")
            .value(new BigDecimal("19.95"))
            .startDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .billingCycle(BillingCycle.builder().id(1L).build())
            .currency(Currency.BRL)
            .active(true)
            .user(otherUser)
            .build()
    );

    // 2. Tenta acessar com token do primeiro usuário
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + authToken)
        .when().get(API_URL + "/{id}", otherSubscription.getId())
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
}

@Test
void delete_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
    authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
    User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

    Subscriptions otherSubscription = subscriptionsRepository.save(
        Subscriptions.builder()
            .name("Spotify")
            .value(new BigDecimal("19.95"))
            .startDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .billingCycle(BillingCycle.builder().id(1L).build())
            .currency(Currency.BRL)
            .active(true)
            .user(otherUser)
            .build()
    );

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + authToken)
        .when().delete(API_URL + "/{id}", otherSubscription.getId())
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());

    // Verifica que a subscription ainda existe
    assertThat(subscriptionsRepository.findById(otherSubscription.getId())).isPresent();
}

@Test
void update_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
    authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
    User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

    Subscriptions otherSubscription = subscriptionsRepository.save(
        Subscriptions.builder()
            .name("Spotify")
            .value(new BigDecimal("19.95"))
            .startDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .billingCycle(BillingCycle.builder().id(1L).build())
            .currency(Currency.BRL)
            .active(true)
            .user(otherUser)
            .build()
    );

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + authToken)
        .body(createSubscriptionRequestDTO())
        .when().put(API_URL + "/{id}", otherSubscription.getId())
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
}
```

---

## Checklist de Implementação

### Setup

- [ ] Unificar `@BeforeEach` em um único método
- [ ] Adicionar limpeza de `refreshTokenRepository`
- [ ] Guardar `authenticatedUser` (não só userId)
- [ ] Garantir ordem de limpeza: subscriptions → refreshTokens → users

### Helpers

- [ ] Remover overload `createSubscription` sem user
- [ ] Atualizar todos os `createSubscription` para usar `authenticatedUser`
- [ ] Atualizar `insertSampleSubscriptions` para usar `authenticatedUser`

### Testes Existentes (corrigir)

- [ ] `findAll_ShouldFilterByName_CaseInsensitivePartialMatch`
- [ ] `findAll_ShouldFilterByActiveStatus`
- [ ] `findAll_ShouldFilterByCategoryId`
- [ ] `findAll_ShouldFilterByCombinedParameters`
- [ ] `findAll_ShouldReturnAllSubscriptions_WhenNoFiltersProvided`

### Testes Novos (adicionar)

- [ ] `findAll_ShouldReturn401_WhenNoTokenProvided`
- [ ] `findById_ShouldReturn401_WhenNoTokenProvided`
- [ ] `create_ShouldReturn401_WhenNoTokenProvided`
- [ ] `update_ShouldReturn401_WhenNoTokenProvided`
- [ ] `delete_ShouldReturn401_WhenNoTokenProvided`
- [ ] `findAll_ShouldNotReturnSubscriptionsFromOtherUsers`
- [ ] `findById_ShouldReturn404_WhenSubscriptionBelongsToOtherUser`
- [ ] `delete_ShouldReturn404_WhenSubscriptionBelongsToOtherUser`
- [ ] `update_ShouldReturn404_WhenSubscriptionBelongsToOtherUser`

---

## Estrutura Final do Arquivo

```
SubscriptionsControllerIT
│
├── CONSTANTS
│   └── API_URL
│
├── DEPENDENCIES (@Autowired)
│   ├── subscriptionsRepository
│   ├── userRepository
│   ├── refreshTokenRepository
│   └── authService
│
├── STATE
│   ├── authToken
│   └── authenticatedUser
│
├── SETUP
│   └── setUp()
│
├── HELPERS
│   ├── createSubscription(name, value, active, category)
│   ├── createSubscriptionRequestDTO()
│   └── insertSampleSubscriptions()
│
├── TESTES DE AUTENTICAÇÃO (401)
│   ├── findAll_ShouldReturn401_WhenNoTokenProvided
│   ├── findById_ShouldReturn401_WhenNoTokenProvided
│   ├── create_ShouldReturn401_WhenNoTokenProvided
│   ├── update_ShouldReturn401_WhenNoTokenProvided
│   └── delete_ShouldReturn401_WhenNoTokenProvided
│
├── TESTES DE ISOLAMENTO (User A vs User B)
│   ├── findAll_ShouldNotReturnSubscriptionsFromOtherUsers
│   ├── findById_ShouldReturn404_WhenSubscriptionBelongsToOtherUser
│   ├── update_ShouldReturn404_WhenSubscriptionBelongsToOtherUser
│   └── delete_ShouldReturn404_WhenSubscriptionBelongsToOtherUser
│
├── TESTES DE CRUD
│   ├── findAll_ShouldReturnAllSubscriptions
│   ├── findById_ShouldReturnOneSubscriptionById
│   ├── findById_ShouldThrowNotFoundException
│   ├── create_ShouldCreateANewSubscription
│   ├── update_ShouldUpdatedSubscriptions
│   ├── update_ShouldThrowNotFoundException
│   ├── delete_ShouldDeleteSubscription
│   └── delete_ShouldThrowNotFoundException
│
├── TESTES DE VALIDAÇÃO
│   ├── create_ShouldReturnBadRequest_WhenValidationFails
│   ├── update_ShouldReturnBadRequest_WhenValidationFails
│   ├── create_ShouldReturnBadRequest_WhenCurrencyIsInvalid
│   └── update_ShouldReturnBadRequest_WhenCurrencyIsInvalid
│
└── TESTES DE FILTRO
    ├── findAll_ShouldFilterByName_CaseInsensitivePartialMatch
    ├── findAll_ShouldFilterByActiveStatus
    ├── findAll_ShouldFilterByCategoryId
    ├── findAll_ShouldFilterByCombinedParameters
    └── findAll_ShouldReturnAllSubscriptions_WhenNoFiltersProvided
```
